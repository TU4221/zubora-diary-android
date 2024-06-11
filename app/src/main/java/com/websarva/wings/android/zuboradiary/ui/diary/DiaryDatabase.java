package com.websarva.wings.android.zuboradiary.ui.diary;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Diary.class}, version = 2, exportSchema = false)
public abstract class DiaryDatabase extends RoomDatabase {
    private static DiaryDatabase _instance;
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    public static DiaryDatabase getDatabase(Context context) {
        if (_instance == null) {
            _instance = Room.databaseBuilder(context.getApplicationContext(), DiaryDatabase.class, "diary_db")
                    //.createFromAsset("database/myapp.db")
                    .build();
        }
        return _instance;
    }

    public abstract DiaryDAO createDiaryDAO();
}
