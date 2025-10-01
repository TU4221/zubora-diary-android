package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
}
