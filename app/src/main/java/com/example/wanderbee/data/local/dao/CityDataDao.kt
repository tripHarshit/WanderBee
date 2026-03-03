package com.example.wanderbee.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wanderbee.data.local.entity.CityDataEntity

@Dao
interface CityDataDao {
    
    @Query("SELECT * FROM city_data WHERE cityKey = :cityKey")
    suspend fun getCityData(cityKey: String): CityDataEntity?
    
    @Query("SELECT * FROM city_data WHERE cityName = :cityName AND countryName = :countryName")
    suspend fun getCityDataByName(cityName: String, countryName: String): CityDataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCityData(cityData: CityDataEntity)
    
    @Query("DELETE FROM city_data WHERE createdAt < :timestamp")
    suspend fun deleteOldData(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM city_data")
    suspend fun getCityDataCount(): Int
    
    @Query("DELETE FROM city_data")
    suspend fun deleteAllCityData()
    
    @Query("DELETE FROM city_data WHERE cityKey = :cityKey")
    suspend fun deleteCityData(cityKey: String)
} 