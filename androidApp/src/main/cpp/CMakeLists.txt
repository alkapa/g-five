# Camino Oboe (solo con -PenableOboe=true). Requiere NDK + prefab.
cmake_minimum_required(VERSION 3.22.1)
project(circuloaudio)

find_package(oboe REQUIRED CONFIG)

add_library(circuloaudio SHARED native_audio.cpp)

target_compile_features(circuloaudio PRIVATE cxx_std_17)
target_link_libraries(circuloaudio PRIVATE oboe::oboe log)
