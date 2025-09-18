package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction

// TODO:引数名へんなのがあるので変更、トランザクション内でDaoを再度生成しているので改善
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
    version = 1,
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
     * 保存する日記データと同じ日付の日記データを削除し、保存する日記データと日記項目タイトル選択履歴データをトランザクション内で保存する。
     *
     * まず保存する日記データと同じ日付の日記を削除し、その後保存する日記を挿入する。
     *
     * @param createDiaryEntity 新しく保存する日記データ。
     */
    @Transaction
    suspend fun deleteAndSaveDiary(
        createDiaryEntity: DiaryEntity,
    ) {
        createDiaryDao().deleteDiary(createDiaryEntity.date)
        createDiaryDao().insertDiary(createDiaryEntity)
    }

    /**
     * 日記項目タイトル選択履歴データをトランザクション内で保存する。
     *
     * 選択履歴を更新し、その後、最新の50件を除く最終使用日時が古い順の履歴を削除する。
     *
     * @param historyList 更新する日記項目タイトル選択履歴データのリスト。
     */
    @Transaction
    suspend fun updateDiaryItemTitleSelectionHistory(
        historyList: List<DiaryItemTitleSelectionHistoryEntity>
    ) {
        createDiaryItemTitleSelectionHistoryDao().insertHistory(historyList)
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
