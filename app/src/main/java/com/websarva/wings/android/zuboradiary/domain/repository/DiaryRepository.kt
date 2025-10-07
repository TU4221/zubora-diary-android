package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.RawWordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle.DiaryItemTitleSelectionHistoryListItem
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.model.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.DiaryItemTitleSelectionHistoryId
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 日記データおよび関連情報へのアクセスと永続化を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、日記のCRUD操作、日記アイテムのタイトル選択履歴の管理、および日記データの検索機能を提供。
 *
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外([UseCaseException] のサブクラス) をスローする。
 */
internal interface DiaryRepository {

    //region Diary
    /**
     * 指定された日付以降に保存されている日記の数を取得する。
     *
     * @param date 日記数をカウントする期間の開始日。`null` の場合は全期間の日記を対象とする。
     * @return 指定された期間の日記の総数。
     * @throws DataStorageException 指定された日付の日記数の取得に失敗した場合。
     */
    suspend fun countDiaries(date: LocalDate? = null): Int

    /**
     * 指定された日付以降の日記データが存在するかどうかを確認する。
     *
     * @param date 確認する日記の日付。
     * @return 指定された日付の日記が存在すれば `true`、存在しなければ `false`。
     * @throws DataStorageException 日記の存在確認処理に失敗した場合。
     */
    suspend fun existsDiary(date: LocalDate): Boolean

    /**
     * 指定された日付の日記データIDを読み込む。
     *
     * @param date 読み込み対象の日付。
     * @return 指定された日付の日記データID。
     * @throws DataStorageException 日記データの読み込みアクセスに失敗した場合。
     * @throws ResourceNotFoundException 該当の日記データが見つからなかった場合。
     */
    suspend fun loadDiaryId(date: LocalDate): DiaryId

    /**
     * 指定されたIDの日記データを読み込む。
     *
     * @param id 読み込み対象のID。
     * @return 指定された日付の日記データ。
     * @throws DataStorageException 日記データの読み込みアクセスに失敗した場合。
     * @throws ResourceNotFoundException 該当の日記データが見つからなかった場合。
     */
    suspend fun loadDiary(id: DiaryId): Diary

    /**
     * 保存されている日記の中で、最も新しい日付の日記データを読み込む。
     *
     * @return 最新の日記データ。
     * @throws DataStorageException 日記データの読み込みアクセスに失敗した場合。
     * @throws ResourceNotFoundException 該当の日記データが見つからなかった場合。
     */
    suspend fun loadNewestDiary(): Diary

    /**
     * 保存されている日記の中で、最も古い日付の日記データを読み込む。
     *
     * @return 最古の日記データ。
     * @throws DataStorageException 日記データの読み込みアクセスに失敗した場合。
     * @throws ResourceNotFoundException 該当の日記データが見つからなかった場合。
     */
    suspend fun loadOldestDiary(): Diary

    /**
     * 日記リストを読み込む。
     *
     * 指定された条件に基づいて日記のリストを取得する。
     * 基準日が `null` の場合は、最新の日記から降順に取得する。
     * 基準日が指定された場合は、その日付以前の日記を降順に取得する。
     *
     * @param num 度に読み込む日記のアイテム数
     * @param offset 読み込みを開始するオフセット。
     * @param date 日記を読み込む期間の開始日。`null` の場合は全期間を対象とする。
     * @return 読み込まれた日記リスト ([DiaryDayListItem.Standard])。条件に合う日記がない場合は空のリスト。
     * @throws DataStorageException 日記リストの読み込みに失敗した場合。
     */
    suspend fun loadDiaryList(
        num: Int,
        offset: Int,
        date: LocalDate?
    ): List<DiaryDayListItem.Standard>

    /**
     * 新しい日記データを保存する。
     *
     * @param diary 保存する日記データ。
     * @throws DataStorageException 日記データの保存に失敗した場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    suspend fun saveDiary(diary: Diary)

    /**
     * 指定されたIDの日記データを削除し、新しい日記データを保存する。
     *
     * 主に、既存の日記と日付が重複する新しい日記を保存する際に使用する。
     *
     * この操作はトランザクションで処理される。
     *
     * @param deleteDiaryId 削除する日記のID。
     * @param saveDiary 保存する日記データ。
     * @throws DataStorageException 日記の削除または保存に失敗した場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    suspend fun deleteAndSaveDiary(deleteDiaryId: DiaryId, saveDiary: Diary)

    /**
     * 指定されたIDの日記データを削除する。
     *
     * @param id 削除対象の日記のID。
     * @throws DataStorageException 日記データの削除に失敗した場合。
     */
    suspend fun deleteDiary(id: DiaryId)

    /**
     * 保存されているすべての日記データを削除する。
     *
     * @throws DataStorageException 全日記データの削除に失敗した場合。
     */
    suspend fun deleteAllDiaries()
    //endregion

    //region WordSearchResult
    /**
     * 指定された検索ワードに一致する日記の数を取得する。
     *
     * @param searchWord 検索する単語。
     * @return 検索ワードに一致した日記の数。
     * @throws DataStorageException 検索結果数の取得に失敗した場合。
     */
    suspend fun countWordSearchResults(searchWord: String): Int

    /**
     * 指定された検索ワードに一致する日記のリストを読み込む。
     *
     * @param num 取得する検索結果の最大数。
     * @param offset 取得開始位置のオフセット。
     * @param searchWord 検索する単語。
     * @return 読み込まれた検索結果のリスト。条件に合う日記がない場合は空のリスト。
     * @throws DataStorageException 検索結果リストの読み込みに失敗した場合。
     */
    suspend fun loadWordSearchResultList(
        num: Int,
        offset: Int,
        searchWord: String
    ): List<RawWordSearchResultListItem>
    //endregion

    //region DiaryItemTitleSelectionHistory
    /**
     * 指定されたタイトルのリストに一致する選択履歴をすべて取得する。
     *
     * @param titleList 検索する履歴のタイトルのリスト。
     * @return 指定されたタイトルに一致した履歴のリスト。一致する履歴がない場合は空のリストを返す。
     * @throws DataStorageException 検索結果リストの読み込みに失敗した場合。
     */
    suspend fun findDiaryItemTitleSelectionHistoriesByTitles(
        titleList: List<String>
    ): List<DiaryItemTitleSelectionHistoryListItem>

    /**
     * 日記アイテムのタイトル選択履歴リストを読み込む。
     *
     * このメソッドは Flow を返し、履歴データの変更を継続的に監視することができます。
     *
     * @param num 取得する履歴アイテムの最大数。
     * @param offset 取得開始位置のオフセット。
     * @return 読み込まれた日記アイテムのタイトル選択履歴リスト ([DiaryItemTitleSelectionHistoryListItem]) を放出する Flow。
     *         条件に合う履歴がない場合は空のリストを放出する。
     * @throws DataStorageException 履歴リストの読み込みに失敗した場合。
     *   ([Flow] 内部で発生する可能性がある)
     *
     */
    fun loadDiaryItemTitleSelectionHistoryList(
        num: Int, offset: Int
    ): Flow<List<DiaryItemTitleSelectionHistoryListItem>>

    /**
     * 日記項目タイトル選択履歴を更新する。
     * 同時に 最新の50件を除く最終使用日時が古い履歴を削除する。
     *
     * この操作はトランザクションで処理される。
     *
     * @param historyItemList 更新する日記アイテムのタイトル選択履歴リスト。
     * @throws DataStorageException 履歴の更新に失敗した場合。
     * @throws InsufficientStorageException ストレージ容量が不足している場合。
     */
    suspend fun updateDiaryItemTitleSelectionHistory(
        historyItemList: List<DiaryItemTitleSelectionHistory>
    )

    /**
     * 指定されたIDの日記アイテム選択履歴を削除する。
     *
     * @param id 削除対象の履歴のタイトルのID。
     * @throws DataStorageException 履歴アイテムの削除に失敗した場合。
     */
    suspend fun deleteDiaryItemTitleSelectionHistory(id: DiaryItemTitleSelectionHistoryId)
    //endregion

    //region Options
    /**
     * アプリケーションの全データ（日記、履歴などすべて）を削除する。
     *
     * @throws DataStorageException 全データの削除に失敗した場合。
     */
    suspend fun deleteAllData()
    //endregion
}
