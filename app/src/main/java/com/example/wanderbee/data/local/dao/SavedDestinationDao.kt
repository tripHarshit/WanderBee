package com.example.wanderbee.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wanderbee.data.local.entity.SavedDestinationEntity

@Dao
interface SavedDestinationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDestination(destination: SavedDestinationEntity)

    @Query("DELETE FROM saved_destinations WHERE destinationId = :destinationId AND userId = :userId")
    suspend fun unsaveDestination(destinationId: String, userId: String)

    @Query("SELECT * FROM saved_destinations WHERE destinationId = :destinationId AND userId = :userId")
    suspend fun getSavedDestination(destinationId: String, userId: String): SavedDestinationEntity?

    @Query("SELECT * FROM saved_destinations WHERE userId = :userId ORDER BY savedAt DESC")
    suspend fun getAllSavedDestinations(userId: String): List<SavedDestinationEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_destinations WHERE destinationId = :destinationId AND userId = :userId)")
    suspend fun isDestinationSaved(destinationId: String, userId: String): Boolean
    
    @Query("DELETE FROM saved_destinations")
    suspend fun deleteAllSavedDestinations()
    
    @Query("SELECT COUNT(*) FROM saved_destinations")
    suspend fun getSavedDestinationsCount(): Int
    
    @Query("DELETE FROM saved_destinations WHERE userId = :userId")
    suspend fun deleteSavedDestinationsByUserId(userId: String)
} 