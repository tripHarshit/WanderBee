// NetworkModule.kt
package com.example.wanderbee.di

import com.example.wanderbee.data.remote.RetrofitInstance
import com.example.wanderbee.data.remote.apiService.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGeoDbApiService(): GeoDbApiService = RetrofitInstance.geoDbApi

    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService = RetrofitInstance.weatherApi

    @Provides
    @Singleton
    fun provideHuggingFaceApiService(): HuggingFaceApiService = RetrofitInstance.huggingFaceApi

    @Provides
    @Singleton
    fun providePexelsApiService(): PexelsApiService = RetrofitInstance.pexelsApi
}
