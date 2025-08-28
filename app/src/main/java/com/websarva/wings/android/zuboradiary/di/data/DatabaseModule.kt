package com.websarva.wings.android.zuboradiary.di.data

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.websarva.wings.android.zuboradiary.data.database.DiaryDao
import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Singleton
    @Provides
    fun provideDiaryDatabase(@ApplicationContext context: Context): DiaryDatabase =
        databaseBuilder(context, DiaryDatabase::class.java, "diary_db")
            .fallbackToDestructiveMigration() // TODO:最終的に削除
            .build()

    @Singleton
    @Provides
    fun provideDiaryDao(diaryDatabase: DiaryDatabase): DiaryDao =
        diaryDatabase.createDiaryDao()

    @Singleton
    @Provides
    fun provideSelectedItemTitlesHistoryDao(diaryDatabase: DiaryDatabase): DiaryItemTitleSelectionHistoryDao =
        diaryDatabase.createDiaryItemTitleSelectionHistoryDao()

    @Singleton
    @Provides
    fun provideDiaryDataSource(
        diaryDatabase: DiaryDatabase,
        diaryDao: DiaryDao,
        diaryItemTitleSelectionHistoryDao: DiaryItemTitleSelectionHistoryDao
    ): DiaryDataSource =
        DiaryDataSource(
            diaryDatabase,
            diaryDao,
            diaryItemTitleSelectionHistoryDao
        )
}
