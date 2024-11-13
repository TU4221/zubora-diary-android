package com.websarva.wings.android.zuboradiary.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// MEMO:テーブル構成変更手順
//      https://qiita.com/kazuma_f/items/8c15e7087623e8f6706b
/*@Database(entities =  {DiaryEntity.class, DiaryItemTitleSelectionHistoryItemEntity.class}, version = 4, exportSchema = true,
        autoMigrations = {@AutoMigration(from = 3, to = 4, spec = DiaryDatabase.MyAutoMigration.class)})*/
@Database(entities =  {DiaryEntity.class, DiaryItemTitleSelectionHistoryItemEntity.class}, version = 1, exportSchema = false/*,
        autoMigrations = {@AutoMigration(from = 2, to = 3, spec = DiaryDatabase.MyAutoMigration.class)}*/)
public abstract class DiaryDatabase extends RoomDatabase {
    //static class MyAutoMigration implements AutoMigrationSpec{}
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(); // TODO:必要？
    public abstract DiaryDAO createDiaryDAO();
    public abstract DiaryItemTitleSelectionHistoryDAO createDiaryItemTitleSelectionHistoryDAO();
}
