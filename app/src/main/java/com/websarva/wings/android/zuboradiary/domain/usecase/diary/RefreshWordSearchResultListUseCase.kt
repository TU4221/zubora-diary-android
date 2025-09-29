package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.NUM_LOAD_ITEMS
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListRefreshException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 既存のワード検索結果リストを再読み込みし、フッターを更新するユースケース。
 *
 * 日記の編集、削除後にリストを最新の状態に更新するために使用される。
 * 読み込むアイテム数は、現在のリストのアイテム数、または最低読み込み数のうち大きい方が採用される。
 *
 * @property loadWordSearchResultListUseCase ワード検索結果リストを読み込むためのユースケース。
 * @property updateWordSearchResultListFooterUseCase ワード検索結果リストのフッターを更新するためのユースケース。
 */
internal class RefreshWordSearchResultListUseCase(
    private val loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
    private val updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "ワード検索結果リスト再読込_"

    /**
     * ユースケースを実行し、ワード検索結果リストを再読み込みし、フッターを更新した新しいリストを返す。
     *
     * @param currentList 現在表示されているワード検索結果のリスト。
     * @param searchWord 検索ワード。
     * @return 処理に成功した場合は [UseCaseResult.Success] に新しい検索結果リスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [WordSearchResultListRefreshException] を格納して返す。
     */
    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.WordSearchResult>,
        searchWord: String
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>, WordSearchResultListRefreshException> {
        Log.i(
            logTag,
            "${logMsg}開始 (現リスト件数: ${currentList.countDiaries()}," +
                    " 読込予定件数: ${currentList.countDiaries()}, 検索ワード: \"$searchWord\")")

        return try {
            var numLoadItems = currentList.countDiaries()

            // HACK:リストが空の状態、又は画面サイズより少ないアイテム数で日記を追加し、
            //      リスト画面に戻った際に以下の問題が発生する回避策。
            //      問題点:
            //      1. 新しく追加された日記が表示されず、追加前のアイテム数でリストが描画される。
            //      2. スクロールによる追加読み込みも機能しない。
            //      対策:
            //      NUM_LOAD_ITEMS に満たない場合は、強制的に NUM_LOAD_ITEMS 分の読み込みを行うことで、
            //      新規追加されたアイテムの表示とスクロール更新を可能にする。
            if (numLoadItems < NUM_LOAD_ITEMS) {
                numLoadItems = NUM_LOAD_ITEMS
            }

            val loadedDiaryList =
                loadDiaryList(
                    numLoadItems,
                    searchWord
                )
            val resultList = updateDiaryListFooter(loadedDiaryList, searchWord)

            Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${resultList.countDiaries()})")
            UseCaseResult.Success(resultList)
        } catch (e: WordSearchResultListLoadException) {
            when (e) {
                is WordSearchResultListLoadException.LoadFailure -> {
                    Log.e(logTag, "${logMsg}失敗_再読込エラー", e)
                    UseCaseResult.Failure(
                        WordSearchResultListRefreshException.RefreshFailure(e)
                    )
                }
                is WordSearchResultListLoadException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        WordSearchResultListRefreshException.Unknown(e)
                    )
                }
            }
        } catch (e: WordSearchListFooterUpdateException) {
            when (e) {
                is WordSearchListFooterUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_フッター更新エラー", e)
                    UseCaseResult.Failure(
                        WordSearchResultListRefreshException.RefreshFailure(e)
                    )
                }
                is WordSearchListFooterUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        WordSearchResultListRefreshException.Unknown(e)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                WordSearchResultListRefreshException.Unknown(e)
            )
        }
    }

    /**
     * 指定されたアイテム数でワード検索結果リストを読み込む。
     *
     * @param numLoadItems 読み込む検索結果のアイテム数。
     * @param searchWord 検索ワード。
     * @return 読み込まれたワード検索結果のリスト。
     * @throws WordSearchResultListLoadException ワード検索結果の読込に失敗した場合。
     */
    private suspend fun loadDiaryList(
        numLoadItems: Int,
        searchWord: String
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        val result =
            loadWordSearchResultListUseCase(
                numLoadItems,
                0, // 再読み込みのためオフセットは0
                searchWord
            )
        return when (result) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    /**
     * 指定されたワード検索結果リストのフッター情報を更新する。
     *
     * @param list フッターを更新する対象のワード検索結果リスト。
     * @param searchWord 検索ワード（フッターの内容決定に使用）。
     * @return フッターが更新されたワード検索結果リスト。
     * @throws WordSearchListFooterUpdateException フッターの更新処理に失敗した場合。
     */
    private suspend fun updateDiaryListFooter(
        list: DiaryYearMonthList<DiaryDayListItem.WordSearchResult>,
        searchWord: String
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        return when (val result = updateWordSearchResultListFooterUseCase(list, searchWord)) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }
}
