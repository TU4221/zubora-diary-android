package com.websarva.wings.android.zuboradiary.ui.diary;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;

import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.SelectedDiaryItemTitle;
import com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle.SelectedItemTitlesHistoryDAO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities =  {Diary.class, SelectedDiaryItemTitle.class}, version = 4, exportSchema = true,
        autoMigrations = {@AutoMigration(from = 3, to = 4, spec = DiaryDatabase.MyAutoMigration.class)})
public abstract class DiaryDatabase extends RoomDatabase {
    static class MyAutoMigration implements AutoMigrationSpec{}
    private static DiaryDatabase _instance;
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    public static DiaryDatabase getDatabase(Context context) {
        if (_instance == null) {
            _instance = Room.databaseBuilder(context.getApplicationContext(), DiaryDatabase.class, "diary_db")
                    //.createFromAsset("database/myapp.db") //データベース初期化:https://www.bedroomcomputing.com/2020/06/2020-0627-db-prepopulate/
                    .build();
        }
        return _instance;
    }

    public abstract DiaryDAO createDiaryDAO();
    public abstract SelectedItemTitlesHistoryDAO createSelectedItemTitlesHistoryDAO();
}
