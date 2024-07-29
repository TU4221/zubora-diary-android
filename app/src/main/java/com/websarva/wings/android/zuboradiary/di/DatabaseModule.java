package com.websarva.wings.android.zuboradiary.di;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO;
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase;
import com.websarva.wings.android.zuboradiary.data.database.SelectedItemTitlesHistoryDAO;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    @Singleton
    @Provides
    public static DiaryDatabase provideDiaryDatabase(@ApplicationContext Context context) {
        Log.d("DatabaseModule", "DatabaseModule#provideDiaryDatabase()");
        return Room
                .databaseBuilder(context, DiaryDatabase.class, "diary_db")
                // MEMO:データベース初期化
                //      https://www.bedroomcomputing.com/2020/06/2020-0627-db-prepopulate/
                .createFromAsset("database/diary_db.db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    public static DiaryDAO provideDiaryDAO(DiaryDatabase diaryDatabase) {
        return diaryDatabase.createDiaryDAO();
    }

    @Singleton
    @Provides
    public static SelectedItemTitlesHistoryDAO provideSelectedItemTitlesHistoryDAO(DiaryDatabase diaryDatabase) {
        return diaryDatabase.createSelectedItemTitlesHistoryDAO();
    }
}
