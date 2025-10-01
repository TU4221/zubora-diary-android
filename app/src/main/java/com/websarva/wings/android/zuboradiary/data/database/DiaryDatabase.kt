package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters

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
@TypeConverters(DiaryDatabaseConverter::class)
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
     * @param saveDiary 新しく保存する日記データ。
     */
    @Transaction
    suspend fun deleteAndSaveDiary(
        saveDiary: DiaryEntity,
    ) {
        createDiaryDao().deleteDiary(saveDiary.date)
        createDiaryDao().insertDiary(saveDiary)
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
}
