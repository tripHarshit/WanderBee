package com.example.wanderbee.di

import com.example.wanderbee.data.repository.DestinationsRepository
import com.example.wanderbee.data.repository.DestinationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindDestinationsRepository(
        impl: DestinationRepository
    ): DestinationsRepository
}
