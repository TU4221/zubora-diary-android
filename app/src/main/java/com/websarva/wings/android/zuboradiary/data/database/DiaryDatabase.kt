package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// MEMO:テーブル構成変更手順
//      https://qiita.com/kazuma_f/items/8c15e7087623e8f6706b
/*@Database(entities =  {DiaryEntity.class, DiaryItemTitleSelectionHistoryItemEntity.class}, version = 4, exportSchema = true,
        autoMigrations = {@AutoMigration(from = 3, to = 4, spec = DiaryDatabase.MyAutoMigration.class)})*/
@Database(
    entities = [DiaryEntity::class, DiaryItemTitleSelectionHistoryItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DiaryDatabase : RoomDatabase() {

    companion object {
        //static class MyAutoMigration implements AutoMigrationSpec{}
        @JvmField
        val EXECUTOR_SERVICE: ExecutorService = Executors.newSingleThreadExecutor()
    }

    abstract fun createDiaryDAO(): DiaryDAO
    abstract fun createDiaryItemTitleSelectionHistoryDAO(): DiaryItemTitleSelectionHistoryDAO
}
