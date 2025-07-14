package com.example.wanderbee.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wanderbee.data.local.entity.ProfileEntity

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): ProfileEntity?

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM user_profiles WHERE userId = :userId)")
    suspend fun profileExists(userId: String): Boolean
} 