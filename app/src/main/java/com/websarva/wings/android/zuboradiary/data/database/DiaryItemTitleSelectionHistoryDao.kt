package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 日記項目のタイトル選択履歴データベースへのアクセスを提供するDAO (Data Access Object) インターフェース。
 *
 * このインターフェースは、"diary_item_title_selection_history" テーブルに対する操作を定義する。
 */
@Dao
internal interface DiaryItemTitleSelectionHistoryDao {
    // @Query使用方法下記参照
    // https://developer.android.com/reference/kotlin/androidx/room/Query

    /**
     * タイトル選択履歴を指定された件数・オフセットで、最終使用日時の降順で取得する。
     *
     * 結果はFlowとして監視可能。
     *
     * @param numTitles 取得する履歴の件数。
     * @param offset 取得を開始するオフセット位置。
     * @return タイトル選択履歴エンティティのリストをFlowでラップしたもの。
     */
    @Query("SELECT * FROM diary_item_title_selection_history ORDER BY log DESC LIMIT :numTitles OFFSET :offset")
    fun selectHistoryListOrderByLogDesc(
        numTitles: Int,
        offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryEntity>>

    /**
     * 新しいタイトル選択履歴のリストをデータベースに挿入する。
     *
     * もし同じタイトルの履歴が既に存在する場合は、置き換える。
     *
     * @param diaryItemTitleSelectionHistoryEntityList 挿入するタイトル選択履歴エンティティのリスト。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        diaryItemTitleSelectionHistoryEntityList: List<DiaryItemTitleSelectionHistoryEntity>
    )

    /**
     * 指定されたタイトルの履歴をデータベースから削除する。
     *
     * @param title 削除する履歴のタイトル。
     */
    @Query("DELETE FROM diary_item_title_selection_history WHERE title = :title")
    suspend fun deleteHistory(title: String)

    // MEMO:SQLITEはDELETE ORDER BYが使用できない。
    /*@Query("DELETE FROM diary_item_title_history ORDER BY log DESC LIMIT ((SELECT COUNT(*) FROM diary_item_title_history) - 50) OFFSET 50")*/
    /**
     * 古いタイトル選択履歴を削除する。
     *
     * 最新の50件を除く、最終使用日時が古い順の履歴を削除する。
     */
    @Query("DELETE FROM diary_item_title_selection_history WHERE title " +
            "NOT IN (SELECT title FROM diary_item_title_selection_history ORDER BY log DESC LIMIT 50 OFFSET 0)")
    suspend fun deleteOldHistory()
}
