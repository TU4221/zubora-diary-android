package com.websarva.wings.android.zuboradiary.di

import android.content.Context
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocationModule {

    @Singleton
    @Provides
    fun provideFusedLocationDataSource(
        @ApplicationContext context: Context
    ):  FusedLocationDataSource {
        return FusedLocationDataSource(context)
    }
}
