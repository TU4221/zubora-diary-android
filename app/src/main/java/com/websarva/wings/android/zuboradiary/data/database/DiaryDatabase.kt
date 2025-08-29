package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import java.time.LocalDate

// MEMO:テーブル構成変更手順
//      https://qiita.com/kazuma_f/items/8c15e7087623e8f6706b
/*@Database(entities =  {DiaryEntity.class, DiaryItemTitleSelectionHistoryItemEntity.class}, version = 4, exportSchema = true,
        autoMigrations = {@AutoMigration(from = 3, to = 4, spec = DiaryDatabase.MyAutoMigration.class)})*/
/**
 * アプリケーションのRoomデータベースクラス。
 *
 * この抽象クラスはRoomDatabaseを継承し、DAOのインスタンスを提供する。
 * [DiaryEntity] と [DiaryItemTitleSelectionHistoryEntity] の2つのエンティティを管理する。
 * データベースのバージョンは1である。
 *
 * @see DiaryDao
 * @see DiaryItemTitleSelectionHistoryDao
 */
@Database(
    entities = [DiaryEntity::class, DiaryItemTitleSelectionHistoryEntity::class],
    version = 5, //TODO:version1に変更
    exportSchema = false
)
internal abstract class DiaryDatabase : RoomDatabase() {

    /**
     * [DiaryDao] のインスタンスを生成して返す。
     *
     * @return DiaryDaoのインスタンス。
     */
    abstract fun createDiaryDao(): DiaryDao

    /**
     * [DiaryItemTitleSelectionHistoryDao] のインスタンスを生成して返す。
     *
     * @return DiaryItemTitleSelectionHistoryDaoのインスタンス。
     */
    abstract fun createDiaryItemTitleSelectionHistoryDao(): DiaryItemTitleSelectionHistoryDao

    /**
     * 日記データと日記項目タイトル選択履歴データをトランザクション内で保存する。
     *
     * 新しい日記を挿入し、選択履歴を更新後、古い選択履歴を削除する。
     *
     * @param diaryEntity 保存する日記データ。
     * @param updateTitleList 更新する日記項目タイトル選択履歴データのリスト。
     */
    @Transaction
    suspend fun saveDiary(
        diaryEntity: DiaryEntity,
        updateTitleList: List<DiaryItemTitleSelectionHistoryEntity>
    ) {
        createDiaryDao().insertDiary(diaryEntity)
        createDiaryItemTitleSelectionHistoryDao().insertHistory(updateTitleList)
        createDiaryItemTitleSelectionHistoryDao().deleteOldHistory()
    }

    /**
     * 指定された日付の日記を削除し、新しい日記データと日記項目タイトル選択履歴データをトランザクション内で保存する。
     *
     * まず指定された日付の日記を削除し、その後新しい日記を挿入する。
     * 選択履歴も更新し、古い選択履歴を削除する。
     *
     * @param deleteDiaryDate 削除する日記の日付。
     * @param createDiaryEntity 新しく保存する日記データ。
     * @param updateTitleList 更新する日記項目タイトル選択履歴データのリスト。
     */
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

    /**
     * データベース内の全ての日記データと日記項目タイトル選択履歴データをトランザクション内で削除する。
     */
    @Transaction
    suspend fun deleteAllData() {
        createDiaryDao().deleteAllDiaries()
        createDiaryItemTitleSelectionHistoryDao().deleteAllHistory()
    }
}
