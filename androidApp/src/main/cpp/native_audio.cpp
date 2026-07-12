// Síntesis Karplus-Strong sobre Oboe: puerto 1:1 de
// shared/.../domain/audio/KarplusStrongSynth.kt (misma constante de
// amortiguación, mismo mezclador). La agenda de disparos llega ya calculada
// desde Kotlin (PlaybackSchedule); aquí solo se reproduce.

#include <jni.h>
#include <oboe/Oboe.h>

#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <memory>
#include <mutex>
#include <vector>

namespace {

constexpr float kDamping = 0.996f;
constexpr float kEnvelopeDecay = 0.9999f;
constexpr float kSilenceThreshold = 1e-4f;
constexpr float kDefaultAmplitude = 0.5f;
constexpr float kMixGain = 0.35f;
constexpr size_t kMaxVoices = 24;

class KarplusStrongVoice {
public:
    KarplusStrongVoice(int midi, int sampleRate) {
        const double freq = 440.0 * std::pow(2.0, (midi - 69) / 12.0);
        const size_t length = std::max<size_t>(2, static_cast<size_t>(sampleRate / freq));
        delay_.resize(length);
        int32_t state = midi * 1103515245 + 12345;
        for (auto &sample : delay_) {
            state = state * 1103515245 + 12345;
            const auto bits = static_cast<uint32_t>(state) >> 8u & 0xFFFFu;
            sample = (static_cast<float>(bits) / 32768.0f - 1.0f) * kDefaultAmplitude;
        }
        envelope_ = kDefaultAmplitude;
    }

    bool isActive() const { return envelope_ > kSilenceThreshold; }

    float nextSample() {
        const float current = delay_[pos_];
        const size_t next = (pos_ + 1) % delay_.size();
        delay_[pos_] = kDamping * 0.5f * (current + delay_[next]);
        pos_ = next;
        envelope_ = std::max(std::fabs(current), envelope_ * kEnvelopeDecay);
        return current;
    }

private:
    std::vector<float> delay_;
    size_t pos_ = 0;
    float envelope_ = 0.0f;
};

struct Trigger {
    int64_t startSample;
    int midi;
    int eventIndex;
};

struct Program {
    std::vector<Trigger> triggers;
    int64_t totalSamples = 0;
    bool loop = false;
    bool notify = false;
};

float softClip(float x) {
    const float c = std::clamp(x, -1.5f, 1.5f);
    return c - (c * c * c) / 6.75f;
}

class Engine : public oboe::AudioStreamDataCallback {
public:
    Engine() {
        oboe::AudioStreamBuilder builder;
        builder.setFormat(oboe::AudioFormat::Float)
                ->setChannelCount(oboe::ChannelCount::Mono)
                ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
                ->setSharingMode(oboe::SharingMode::Shared)
                ->setDataCallback(this);
        if (builder.openStream(stream_) == oboe::Result::OK) {
            sampleRate_ = stream_->getSampleRate();
            stream_->requestStart();
        }
    }

    ~Engine() override {
        if (stream_) {
            stream_->stop();
            stream_->close();
        }
    }

    int sampleRate() const { return sampleRate_; }

    void submit(std::unique_ptr<Program> program) {
        std::lock_guard<std::mutex> lock(pendingMutex_);
        pending_ = std::move(program);
    }

    void stopPlayback() { stopRequested_.store(true); }

    int currentEventIndex() const { return currentEvent_.load(); }

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream * /*stream*/, void *audioData, int32_t numFrames) override {
        {
            std::lock_guard<std::mutex> lock(pendingMutex_);
            if (pending_) {
                program_ = std::move(pending_);
                position_ = 0;
                nextTrigger_ = 0;
                currentEvent_.store(-1);
            }
        }
        if (stopRequested_.exchange(false) && program_) {
            program_.reset();
            currentEvent_.store(-1);
        }
        if (program_) {
            const auto &triggers = program_->triggers;
            while (nextTrigger_ < triggers.size() &&
                   triggers[nextTrigger_].startSample < position_ + numFrames) {
                const Trigger &t = triggers[nextTrigger_];
                voices_.emplace_back(t.midi, sampleRate_);
                while (voices_.size() > kMaxVoices) voices_.erase(voices_.begin());
                if (program_->notify) currentEvent_.store(t.eventIndex);
                ++nextTrigger_;
            }
            position_ += numFrames;
            if (position_ >= program_->totalSamples) {
                if (program_->loop) {
                    position_ = 0;
                    nextTrigger_ = 0;
                } else {
                    program_.reset();
                    currentEvent_.store(-1);
                }
            }
        }
        auto *out = static_cast<float *>(audioData);
        for (int32_t i = 0; i < numFrames; ++i) {
            float sum = 0.0f;
            for (auto &voice : voices_) sum += voice.nextSample();
            out[i] = softClip(sum * kMixGain);
        }
        voices_.erase(
                std::remove_if(voices_.begin(), voices_.end(),
                               [](const KarplusStrongVoice &v) { return !v.isActive(); }),
                voices_.end());
        return oboe::DataCallbackResult::Continue;
    }

private:
    std::shared_ptr<oboe::AudioStream> stream_;
    int sampleRate_ = 48000;

    std::mutex pendingMutex_;
    std::unique_ptr<Program> pending_;
    std::atomic<bool> stopRequested_{false};
    std::atomic<int> currentEvent_{-1};

    // Estado propiedad del callback de audio.
    std::unique_ptr<Program> program_;
    std::vector<KarplusStrongVoice> voices_;
    int64_t position_ = 0;
    size_t nextTrigger_ = 0;
};

Engine *fromHandle(jlong handle) { return reinterpret_cast<Engine *>(handle); }

}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_alkapa_circuloquintas_audio_OboeAudioEngine_nativeCreate(JNIEnv *, jobject) {
    return reinterpret_cast<jlong>(new Engine());
}

JNIEXPORT jint JNICALL
Java_com_alkapa_circuloquintas_audio_OboeAudioEngine_nativeSampleRate(
        JNIEnv *, jobject, jlong handle) {
    return fromHandle(handle)->sampleRate();
}

JNIEXPORT void JNICALL
Java_com_alkapa_circuloquintas_audio_OboeAudioEngine_nativeSubmit(
        JNIEnv *env, jobject, jlong handle, jlongArray startSamples, jintArray midis,
        jintArray eventIndices, jlong totalSamples, jboolean loop, jboolean notify) {
    const jsize count = env->GetArrayLength(startSamples);
    auto program = std::make_unique<Program>();
    program->triggers.resize(count);
    program->totalSamples = totalSamples;
    program->loop = loop;
    program->notify = notify;

    jlong *starts = env->GetLongArrayElements(startSamples, nullptr);
    jint *midiValues = env->GetIntArrayElements(midis, nullptr);
    jint *indices = env->GetIntArrayElements(eventIndices, nullptr);
    for (jsize i = 0; i < count; ++i) {
        program->triggers[i] = Trigger{starts[i], midiValues[i], indices[i]};
    }
    env->ReleaseLongArrayElements(startSamples, starts, JNI_ABORT);
    env->ReleaseIntArrayElements(midis, midiValues, JNI_ABORT);
    env->ReleaseIntArrayElements(eventIndices, indices, JNI_ABORT);

    fromHandle(handle)->submit(std::move(program));
}

JNIEXPORT void JNICALL
Java_com_alkapa_circuloquintas_audio_OboeAudioEngine_nativeStop(
        JNIEnv *, jobject, jlong handle) {
    fromHandle(handle)->stopPlayback();
}

JNIEXPORT jint JNICALL
Java_com_alkapa_circuloquintas_audio_OboeAudioEngine_nativeCurrentEventIndex(
        JNIEnv *, jobject, jlong handle) {
    return fromHandle(handle)->currentEventIndex();
}

JNIEXPORT void JNICALL
Java_com_alkapa_circuloquintas_audio_OboeAudioEngine_nativeRelease(
        JNIEnv *, jobject, jlong handle) {
    delete fromHandle(handle);
}

}  // extern "C"
