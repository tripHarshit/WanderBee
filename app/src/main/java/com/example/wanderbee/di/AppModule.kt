package com.example.wanderbee.di

import com.example.wanderbee.data.remote.apiService.HuggingFaceApiService
import com.example.wanderbee.data.repository.DefaultHuggingFaceRepository
import com.example.wanderbee.data.repository.HuggingFaceRepository
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
}