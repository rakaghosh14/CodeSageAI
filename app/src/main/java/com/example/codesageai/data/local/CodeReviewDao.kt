package com.example.codesageai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: CodeReviewEntity): Long

    @Query("SELECT * FROM code_reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<CodeReviewEntity>>

    @Query("SELECT * FROM code_reviews WHERE id = :id")
    fun getReviewById(id: Long): Flow<CodeReviewEntity?>

    @Query("DELETE FROM code_reviews WHERE id = :id")
    suspend fun deleteReviewById(id: Long)

    @Query("SELECT * FROM code_reviews WHERE title LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR language LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchReviews(query: String): Flow<List<CodeReviewEntity>>
}
