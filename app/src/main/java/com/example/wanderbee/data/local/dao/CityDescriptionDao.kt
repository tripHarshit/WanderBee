package com.example.wanderbee.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wanderbee.data.local.entity.CityDescriptionEntity

@Dao
interface CityDescriptionDao {
    @Query("SELECT * FROM city_descriptions WHERE cityName = :cityName")
    suspend fun getDescription(cityName: String): CityDescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDescription(description: CityDescriptionEntity)

    @Query("DELETE FROM city_descriptions WHERE timestamp < :timestamp")
    suspend fun deleteOldDescriptions(timestamp: Long)

    @Query("DELETE FROM city_descriptions WHERE cityName = :cityName")
    suspend fun deleteOldDescriptions(cityName: String)
    
    @Query("DELETE FROM city_descriptions")
    suspend fun deleteAllCityDescriptions()
}