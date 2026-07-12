package com.alkapa.circuloquintas.data.db

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "progressions")
data class ProgressionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val tonicLetter: String,
    val tonicAccidental: Int,
    val scaleType: String,
    val bpm: Int,
    val readOnly: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "progression_chords",
    foreignKeys = [
        ForeignKey(
            entity = ProgressionEntity::class,
            parentColumns = ["id"],
            childColumns = ["progressionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("progressionId")],
)
data class ProgressionChordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val progressionId: Long,
    val position: Int,
    val degreeIndex: Int,
    val quality: String,
    val beats: Int,
)

data class ProgressionWithChords(
    @Embedded val progression: ProgressionEntity,
    @Relation(parentColumn = "id", entityColumn = "progressionId")
    val chords: List<ProgressionChordEntity>,
)

@Dao
interface ProgressionDao {

    @Transaction
    @Query("SELECT * FROM progressions ORDER BY readOnly ASC, updatedAt DESC")
    fun observeAll(): Flow<List<ProgressionWithChords>>

    @Transaction
    @Query("SELECT * FROM progressions WHERE id = :id")
    suspend fun get(id: Long): ProgressionWithChords?

    @Query("SELECT COUNT(*) FROM progressions")
    suspend fun count(): Int

    @Insert
    suspend fun insertProgression(progression: ProgressionEntity): Long

    @Insert
    suspend fun insertChords(chords: List<ProgressionChordEntity>)

    @Update
    suspend fun updateProgression(progression: ProgressionEntity)

    @Query("DELETE FROM progression_chords WHERE progressionId = :progressionId")
    suspend fun deleteChordsOf(progressionId: Long)

    @Query("UPDATE progressions SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rename(id: Long, name: String, updatedAt: Long)

    @Query("DELETE FROM progressions WHERE id = :id")
    suspend fun delete(id: Long)

    @Delete
    suspend fun deleteEntity(progression: ProgressionEntity)

    /** Guardado completo: cabecera + acordes en orden de posición. */
    @Transaction
    suspend fun upsertWithChords(
        progression: ProgressionEntity,
        chords: List<ProgressionChordEntity>,
    ): Long {
        val id = if (progression.id == 0L) {
            insertProgression(progression)
        } else {
            updateProgression(progression)
            deleteChordsOf(progression.id)
            progression.id
        }
        insertChords(chords.mapIndexed { i, c -> c.copy(progressionId = id, position = i) })
        return id
    }
}

@Database(
    entities = [ProgressionEntity::class, ProgressionChordEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun progressionDao(): ProgressionDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "circulo-quintas.db")
                .build()

        fun inMemory(context: Context): AppDatabase =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
