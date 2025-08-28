package com.websarva.wings.android.zuboradiary.di.data

import android.content.Context
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object UriModule {

    @Singleton
    @Provides
    fun provideUriPermissionDataSource(
        @ApplicationContext context: Context
    ):  UriPermissionDataSource = UriPermissionDataSource(context.contentResolver)
}
