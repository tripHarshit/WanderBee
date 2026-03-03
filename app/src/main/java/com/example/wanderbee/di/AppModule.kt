package com.example.wanderbee.di

import android.content.Context
import com.example.wanderbee.data.remote.StompClient
import com.example.wanderbee.data.remote.apiService.ChatApiService
import com.example.wanderbee.data.remote.apiService.DestinationApiService
import com.example.wanderbee.data.remote.apiService.GenerationService
import com.example.wanderbee.data.repository.AiRepository
import com.example.wanderbee.data.repository.ChatRepository
import com.example.wanderbee.data.repository.DefaultAiRepository
import com.example.wanderbee.data.repository.DefaultChatRepository
import com.example.wanderbee.data.repository.DefaultWeatherRepository
import com.example.wanderbee.data.repository.WeatherRepository
import com.example.wanderbee.data.remote.apiService.ImgBBApiService
import com.example.wanderbee.data.remote.apiService.WeatherApiService
import com.example.wanderbee.data.repository.ImgBBRepository
import com.example.wanderbee.data.repository.DefaultImgBBRepository
import com.example.wanderbee.utils.AppPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideWeatherRepository(apiService: WeatherApiService): WeatherRepository{
        return DefaultWeatherRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        chatApiService: ChatApiService,
        appPreferences: AppPreferences
    ): ChatRepository {
        return DefaultChatRepository(chatApiService, appPreferences)
    }

    @Provides
    @Singleton
    fun provideStompClient(appPreferences: AppPreferences): StompClient {
        return StompClient(appPreferences)
    }

    @Provides
    @Singleton
    fun provideAiRepository(
        apiService: DestinationApiService,
        generationService: GenerationService
    ): AiRepository {
        return DefaultAiRepository( generationService)
    }

    @Provides
    @Singleton
    fun provideImgBBRepository(imgBBApiService: ImgBBApiService): ImgBBRepository {
        return DefaultImgBBRepository(imgBBApiService)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}