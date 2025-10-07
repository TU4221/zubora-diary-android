package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import java.time.LocalDate

/**
 * 日記データベースへのアクセスを提供するDAO (Data Access Object) インターフェース。
 *
 * このインターフェースは、"diaries" テーブルに対する操作を定義する。
 */
@Dao
internal interface DiaryDao {
    // MEMO:@Query使用方法下記参照
    //      https://developer.android.com/reference/kotlin/androidx/room/Query

    /**
     * データベースに保存されている日記の総数を取得する。
     *
     * @return 日記の総数。
     */
    @Query("SELECT COUNT(*) FROM diaries")
    suspend fun countDiaries(): Int

    /**
     * 指定された日付より前の日記の総数を取得する。
     *
     * @param startDate この日付以前の日記をカウントする (この日付を含む)。
     * @return 指定された日付より前の日記の総数。
     */
    @Query("SELECT COUNT(*) FROM diaries WHERE date < :startDate")
    suspend fun countDiaries(startDate: LocalDate): Int

    /**
     * 指定された日付の日記が存在するかどうかを確認する。
     *
     * @param date 確認する日記の日付。
     * @return 日記が存在すればtrue、しなければfalse。
     */
    @Query("SELECT EXISTS (SELECT 1 FROM diaries WHERE date = :date)")
    suspend fun existsDiary(date: LocalDate): Boolean

    /**
     * 指定された日付の日記エンティティのIDを取得する。
     *
     * @param date 取得する日記IDの日付。
     * @return 指定された日付のDiaryEntity。見つからない場合はnull。
     */
    @Query("SELECT id FROM diaries WHERE date = :date")
    suspend fun selectDiaryId(date: LocalDate): String?

    /**
     * 指定されたIDの日記エンティティを取得する。
     *
     * @param id 取得する日記のID。
     * @return 指定された日付のDiaryEntity。見つからない場合はnull。
     */
    @Query("SELECT * FROM diaries WHERE id = :id")
    suspend fun selectDiary(id: String): DiaryEntity?

    /**
     * 最新の日記エンティティを取得する。
     *
     * @return 最新のDiaryEntity。日記が存在しない場合はnull。
     */
    @Query("SELECT * FROM diaries ORDER BY date DESC LIMIT 1 OFFSET 0")
    suspend fun selectNewestDiary(): DiaryEntity?

    /**
     * 最古の日記エンティティを取得する。
     *
     * @return 最古のDiaryEntity。日記が存在しない場合はnull。
     */
    @Query("SELECT * FROM diaries ORDER BY date ASC LIMIT 1 OFFSET 0")
    suspend fun selectOldestDiary(): DiaryEntity?

    /**
     * 日記リストのデータを日付の降順で指定された件数・オフセットで取得する。
     *
     * @param num 取得する日記の件数。
     * @param offset 取得を開始するオフセット位置。
     * @return 日記リストアイテムデータのリスト。対象の日記が存在しない場合は空のリストを返す。
     */
    @Query("SELECT id, date, title, image_file_name FROM diaries ORDER BY date DESC LIMIT :num OFFSET :offset")
    suspend fun selectDiaryListOrderByDateDesc(
        num: Int,
        offset: Int
    ): List<DiaryListItemData>

    /**
     * 指定された開始日以前の日記リストのデータを日付の降順で指定された件数・オフセットで取得する。
     *
     * @param num 取得する日記の件数。
     * @param offset 取得を開始するオフセット位置。
     * @param startDate この日付以前の日記を取得する (この日付を含む)。
     * @return 日記リストアイテムデータのリスト。対象の日記が存在しない場合は空のリストを返す。
     */
    @Query("SELECT id, date, title, image_file_name FROM diaries WHERE date <= :startDate ORDER BY date DESC LIMIT :num OFFSET :offset")
    suspend fun selectDiaryListOrderByDateDesc(
        num: Int,
        offset: Int,
        startDate: LocalDate
    ): List<DiaryListItemData>

    /**
     * 指定された単語がタイトルまたは各項目に含まれる日記の総数を取得する。
     *
     * 検索対象は、日記のタイトル、各項目のタイトル、コメントである。
     *
     * @param word 検索する単語。
     * @return 検索条件に一致する日記の総数。
     */
    @Query(
        ("SELECT COUNT(*) " +
                "FROM diaries " +
                "WHERE title GLOB '*' || :word || '*' " +
                "OR item_1_title GLOB '*' || :word || '*'" +
                "OR item_1_comment GLOB '*' || :word || '*'" +
                "OR item_2_title GLOB '*' || :word || '*'" +
                "OR item_2_comment GLOB '*' || :word || '*'" +
                "OR item_3_title GLOB '*' || :word || '*'" +
                "OR item_3_comment GLOB '*' || :word || '*'" +
                "OR item_4_title GLOB '*' || :word || '*'" +
                "OR item_4_comment GLOB '*' || :word || '*'" +
                "OR item_5_title GLOB '*' || :word || '*'" +
                "OR item_5_comment GLOB '*' || :word || '*'")
    )
    suspend fun countWordSearchResults(word: String): Int

    /**
     * 指定された単語がタイトルまたは各項目に含まれる日記の検索結果リストを指定された件数・オフセットで取得する。
     *
     * 検索対象は、日記のタイトル、各項目のタイトルコメントである。
     * 結果は日付の降順でソートされる。
     *
     * @param num 取得する日記の件数。
     * @param offset 取得を開始するオフセット位置。
     * @param word 検索する単語。
     * @return 単語検索結果リストアイテムデータのリスト。対象の日記が存在しない場合は空のリストを返す。
     */
    @Query(
        ("SELECT id, date, title, item_1_title, item_1_comment, " +
                "item_2_title, item_2_comment, " +
                "item_3_title, item_3_comment, " +
                "item_4_title, item_4_comment, " +
                "item_5_title, item_5_comment " +
                "FROM diaries " +
                "WHERE title GLOB '*' || :word || '*' " +
                "OR item_1_title GLOB '*' || :word || '*'" +
                "OR item_1_comment GLOB '*' || :word || '*'" +
                "OR item_2_title GLOB '*' || :word || '*'" +
                "OR item_2_comment GLOB '*' || :word || '*'" +
                "OR item_3_title GLOB '*' || :word || '*'" +
                "OR item_3_comment GLOB '*' || :word || '*'" +
                "OR item_4_title GLOB '*' || :word || '*'" +
                "OR item_4_comment GLOB '*' || :word || '*'" +
                "OR item_5_title GLOB '*' || :word || '*'" +
                "OR item_5_comment GLOB '*' || :word || '*'" +
                "ORDER BY date DESC LIMIT :num OFFSET :offset")
    )
    suspend fun selectWordSearchResultListOrderByDateDesc(
        num: Int,
        offset: Int,
        word: String
    ): List<WordSearchResultListItemData>

    /**
     * 新しい日記をデータベースに挿入、または既存の日記を更新する。
     *
     * 渡された日記の主キー(id)がデータベースに存在しない場合は、新しい日記として挿入する。
     * 既に存在する場合は、その日記のデータを更新する。
     *
     * @param diaryEntity 挿入する日記エンティティ。
     */
    @Upsert
    suspend fun upsertDiary(diaryEntity: DiaryEntity)

    /**
     * 指定されたIDの日記をデータベースから削除する。
     *
     * @param id 削除する日記のID。
     */
    @Query("DELETE FROM diaries WHERE id = :id")
    suspend fun deleteDiary(id: String)

    /**
     * 指定されたIDの日記を削除し、新しい日記をデータベースに挿入、または既存の日記を更新する。
     *
     * 内部で [deleteDiary] と [upsertDiary] をトランザクションで処理する。
     *
     * @param deleteDiaryId 削除する日記のID。
     * @param saveDiary 新しく保存する日記データ。
     */
    @Transaction
    suspend fun deleteAndUpsertDiary(
        deleteDiaryId: String,
        saveDiary: DiaryEntity,
    ) {
        deleteDiary(deleteDiaryId)
        upsertDiary(saveDiary)
    }

    /**
     * 全ての日記をデータベースから削除する。
     */
    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()
}
