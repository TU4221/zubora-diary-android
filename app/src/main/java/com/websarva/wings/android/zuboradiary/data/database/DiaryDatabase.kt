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
    entities = [DiaryEntity::class, DiaryItemTitleSelectionHistoryEntity::class],
    version = 5,
    exportSchema = false
)
internal abstract class DiaryDatabase : RoomDatabase() {

    abstract fun createDiaryDao(): DiaryDao
    abstract fun createDiaryItemTitleSelectionHistoryDao(): DiaryItemTitleSelectionHistoryDao

    @Transaction
    suspend fun saveDiary(
        diaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryEntity>
    ) {
        createDiaryDao().insertDiary(diaryEntity)
        createDiaryItemTitleSelectionHistoryDao().insertHistory(updateTitleList)
        createDiaryItemTitleSelectionHistoryDao().deleteOldHistory()
    }

    @Transaction
    suspend fun deleteAndSaveDiary(
        deleteDiaryDate: LocalDate,
        createDiaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryEntity>
    ) {
        createDiaryDao().deleteDiary(deleteDiaryDate.toString())
        createDiaryDao().insertDiary(createDiaryEntity)
        createDiaryItemTitleSelectionHistoryDao().insertHistory(updateTitleList)
        createDiaryItemTitleSelectionHistoryDao().deleteOldHistory()
    }

    @Transaction
    suspend fun deleteAllData() {
        createDiaryDao().deleteAllDiaries()
        createDiaryItemTitleSelectionHistoryDao().deleteAllHistory()
    }
}
