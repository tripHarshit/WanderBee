// NetworkModule.kt
package com.example.wanderbee.di

import com.example.wanderbee.data.remote.AuthInterceptor
import com.example.wanderbee.data.remote.RetrofitInstance
import com.example.wanderbee.data.remote.apiService.ChatApiService
import com.example.wanderbee.data.remote.apiService.DestinationApiService
import com.example.wanderbee.data.remote.apiService.GenerationService
import com.example.wanderbee.data.remote.apiService.IdentityApiService
import com.example.wanderbee.data.remote.apiService.ImgBBApiService
import com.example.wanderbee.data.remote.apiService.PexelsApiService
import com.example.wanderbee.data.remote.apiService.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun providePexelsApiService(
        @AuthenticatedClient authClient: OkHttpClient
    ): PexelsApiService = RetrofitInstance.createPexelsApi(authClient)

    @Provides
    @Singleton
    fun provideWeatherApiService(
        @AuthenticatedClient authClient: OkHttpClient
    ): WeatherApiService = RetrofitInstance.createWeatherApi(authClient)

    @Provides
    @Singleton
    fun provideImgbbApiService(): ImgBBApiService = RetrofitInstance.imgbbApi

    @Provides
    @Singleton
    fun provideIdentityApiService(
        @AuthenticatedClient authClient: OkHttpClient
    ): IdentityApiService = RetrofitInstance.createIdentityApi(authClient)

    @Provides
    @Singleton
    fun provideDestinationApiService(
        @AuthenticatedClient authClient: OkHttpClient
    ): DestinationApiService = RetrofitInstance.createDestinationApi(authClient)

    @Provides
    @Singleton
    fun provideChatApiService(
        @AuthenticatedClient authClient: OkHttpClient
    ): ChatApiService = RetrofitInstance.createChatApi(authClient)

    @Provides
    @Singleton
    fun provideGenerationService(
        @AuthenticatedClient authClient: OkHttpClient
    ): GenerationService = RetrofitInstance.createGenerationService(authClient)

}
