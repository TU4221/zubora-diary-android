package com.websarva.wings.android.zuboradiary.ui.diary;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RenameColumn;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Diary.class}, version = 3, exportSchema = true,
        autoMigrations = {@AutoMigration(from = 2, to = 3, spec = DiaryDatabase.MyAutoMigration.class)})
public abstract class DiaryDatabase extends RoomDatabase {
    @RenameColumn(tableName = "diaries", fromColumnName = "imagePath", toColumnName = "picturePath")
    static class MyAutoMigration implements AutoMigrationSpec{}
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
