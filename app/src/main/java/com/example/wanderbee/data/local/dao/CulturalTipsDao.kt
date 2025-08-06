package com.example.wanderbee.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wanderbee.data.local.entity.CulturalTipsEntity

@Dao
interface CulturalTipsDao {
    @Query("SELECT * FROM cultural_tips WHERE cityName = :cityName")
    suspend fun getTip(cityName: String): CulturalTipsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: CulturalTipsEntity)

    @Query("DELETE FROM cultural_tips WHERE timestamp < :timestamp")
    suspend fun deleteOldTips(timestamp: Long)
    
    @Query("DELETE FROM cultural_tips")
    suspend fun deleteAllCulturalTips()
    
    @Query("SELECT COUNT(*) FROM cultural_tips")
    suspend fun getCulturalTipsCount(): Int
    
    @Query("DELETE FROM cultural_tips WHERE cityName = :cityName")
    suspend fun deleteCulturalTips(cityName: String)
}