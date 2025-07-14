package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import java.time.LocalDate

// MEMO:テーブル構成変更手順
//      https://qiita.com/kazuma_f/items/8c15e7087623e8f6706b
/*@Database(entities =  {DiaryEntity.class, DiaryItemTitleSelectionHistoryItemEntity.class}, version = 4, exportSchema = true,
        autoMigrations = {@AutoMigration(from = 3, to = 4, spec = DiaryDatabase.MyAutoMigration.class)})*/
@Database(
    entities = [DiaryEntity::class, DiaryItemTitleSelectionHistoryItemEntity::class],
    version = 5,
    exportSchema = false
)
internal abstract class DiaryDatabase : RoomDatabase() {

    abstract fun createDiaryDAO(): DiaryDAO
    abstract fun createDiaryItemTitleSelectionHistoryDAO(): DiaryItemTitleSelectionHistoryDAO

    @Transaction
    suspend fun saveDiary(
        diaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ) {
        createDiaryDAO().insertDiary(diaryEntity)
        createDiaryItemTitleSelectionHistoryDAO().insertHistoryItem(updateTitleList)
        createDiaryItemTitleSelectionHistoryDAO().deleteOldHistoryItem()
    }

    @Transaction
    suspend fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate,
        createDiaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryItemEntity>
    ) {
        createDiaryDAO().deleteDiary(deleteDiaryDate.toString())
        createDiaryDAO().insertDiary(createDiaryEntity)
        createDiaryItemTitleSelectionHistoryDAO().insertHistoryItem(updateTitleList)
        createDiaryItemTitleSelectionHistoryDAO().deleteOldHistoryItem()
    }

    @Transaction
    suspend fun deleteAllData() {
        createDiaryDAO().deleteAllDiaries()
        createDiaryItemTitleSelectionHistoryDAO().deleteAllItem()
    }
}
