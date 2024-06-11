package com.websarva.wings.android.zuboradiary.ui.diary.editdiaryselectitemtitle;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//.createFromAsset("database/myapp.db")
@Database(entities = {DiaryItemTitle.class}, version = 2, exportSchema = false)
public abstract class SelectedItemTitlesHistoryDatabase extends RoomDatabase {
    private static SelectedItemTitlesHistoryDatabase _instance;
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    public static SelectedItemTitlesHistoryDatabase getDatabase(Context context) {
        if (_instance == null) {
            _instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    SelectedItemTitlesHistoryDatabase.class,
                    "diary_item_titles_history_db"
                    ).build();
        }
        return _instance;
    }

    public abstract SelectedItemTitlesHistoryDAO createSelectedItemTitlesHistoryDAO();
}
