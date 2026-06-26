package com.example.codesageai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM chat_messages WHERE reviewId = :reviewId ORDER BY timestamp ASC")
    fun getMessagesForReview(reviewId: Long): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_messages WHERE reviewId = :reviewId")
    suspend fun clearMessagesForReview(reviewId: Long)
}
