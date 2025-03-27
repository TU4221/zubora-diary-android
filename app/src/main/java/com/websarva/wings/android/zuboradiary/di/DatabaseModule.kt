package com.websarva.wings.android.zuboradiary.di

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDAO
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
    fun provideDiaryDatabase(@ApplicationContext context: Context): DiaryDatabase {
        return databaseBuilder(context, DiaryDatabase::class.java, "diary_db").build()
    }

    @Singleton
    @Provides
    fun provideDiaryDAO(diaryDatabase: DiaryDatabase): DiaryDAO {
        return diaryDatabase.createDiaryDAO()
    }

    @Singleton
    @Provides
    fun provideSelectedItemTitlesHistoryDAO(diaryDatabase: DiaryDatabase): DiaryItemTitleSelectionHistoryDAO {
        return diaryDatabase.createDiaryItemTitleSelectionHistoryDAO()
    }
}
