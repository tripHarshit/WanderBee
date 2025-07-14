package com.example.wanderbee.di

import com.example.wanderbee.data.remote.apiService.AiApiService
import com.example.wanderbee.data.remote.apiService.HuggingFaceApiService
import com.example.wanderbee.data.remote.apiService.WeatherApiService
import com.example.wanderbee.data.repository.AiRepository
import com.example.wanderbee.data.repository.ChatRepository
import com.example.wanderbee.data.repository.DefaultAiRepository
import com.example.wanderbee.data.repository.DefaultChatRepository
import com.example.wanderbee.data.repository.DefaultHuggingFaceRepository
import com.example.wanderbee.data.repository.DefaultWeatherRepository
import com.example.wanderbee.data.repository.HuggingFaceRepository
import com.example.wanderbee.data.repository.WeatherRepository
import com.example.wanderbee.data.remote.apiService.ImgBBApiService
import com.example.wanderbee.data.repository.ImgBBRepository
import com.example.wanderbee.data.repository.DefaultImgBBRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFireStore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideHuggingFaceRepository(apiService: HuggingFaceApiService): HuggingFaceRepository {
        return DefaultHuggingFaceRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(apiService: WeatherApiService): WeatherRepository{
        return DefaultWeatherRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ChatRepository {
        return DefaultChatRepository(firestore, auth)
    }

    @Provides
   @Singleton
    fun provideAiRepository(apiService: AiApiService): AiRepository {
        return DefaultAiRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideImgBBRepository(imgBBApiService: ImgBBApiService): ImgBBRepository {
        return DefaultImgBBRepository(imgBBApiService)
    }
}