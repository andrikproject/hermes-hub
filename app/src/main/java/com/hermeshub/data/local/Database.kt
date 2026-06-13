package com.hermeshub.data.local

import androidx.room.*
import com.hermeshub.data.model.HermesConnection
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections ORDER BY createdAt DESC")
    fun getAllConnections(): Flow<List<HermesConnection>>

    @Query("SELECT * FROM connections WHERE id = :id")
    suspend fun getConnectionById(id: Long): HermesConnection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: HermesConnection): Long

    @Update
    suspend fun updateConnection(connection: HermesConnection)

    @Delete
    suspend fun deleteConnection(connection: HermesConnection)

    @Query("UPDATE connections SET isOnline = :isOnline WHERE id = :id")
    suspend fun updateOnlineStatus(id: Long, isOnline: Boolean)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE connectionId = :connectionId ORDER BY timestamp ASC")
    fun getMessagesForConnection(connectionId: Long): Flow<List<com.hermeshub.data.model.ChatMessage>>

    @Insert
    suspend fun insertMessage(message: com.hermeshub.data.model.ChatMessage): Long

    @Query("DELETE FROM messages WHERE connectionId = :connectionId")
    suspend fun deleteMessagesForConnection(connectionId: Long)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)
}

@Database(
    entities = [HermesConnection::class, com.hermeshub.data.model.ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class HermesDatabase : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: HermesDatabase? = null

        fun getDatabase(
            context: android.content.Context
        ): HermesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HermesDatabase::class.java,
                    "hermes_hub_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
