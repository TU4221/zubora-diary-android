package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.NUM_LOAD_ITEMS
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListAdditionLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListLoadException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.utils.createLogTag
/**
 * 既存のワード検索結果リストに追加の検索結果データを読み込み、結合してフッターを更新するユースケース。
 *
 * ワード検索結果リストの末尾までスクロールした際に、追加の検索結果を読み込むために使用される。
 *
 * @property loadWordSearchResultListUseCase ワード検索結果リストを読み込むためのユースケース。
 * @property updateWordSearchResultListFooterUseCase ワード検索結果リストのフッターを更新するためのユースケース。
 */
internal class LoadAdditionWordSearchResultListUseCase(
    private val loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
    private val updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "追加ワード検索結果リスト読込_"

    /**
     * ユースケースを実行し、現在のリストに追加の検索結果を読み込み、フッターを更新した新しいリストを返す。
     *
     * @param currentList 現在表示されているワード検索結果のリスト。
     * @param searchWord 検索ワード。
     * @return 追加読み込みとフッター更新が成功した場合は、新しい検索結果リストを [UseCaseResult.Success] に格納して返す。
     *   処理中にエラーが発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.WordSearchResult>,
        searchWord: String
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>, WordSearchResultListAdditionLoadException> {
        Log.i(logTag, "${logMsg}開始 (現リスト件数: ${currentList.countDiaries()}, 検索ワード: \"$searchWord\")")

        val loadedDiaryList =
            try {
                loadDiaryList(
                    currentList.countDiaries(),
                    searchWord
                )
            } catch (e: WordSearchResultListLoadException) {
                Log.e(logTag, "${logMsg}失敗_追加ワード検索結果読込処理エラー", e)
                return UseCaseResult.Failure(
                    WordSearchResultListAdditionLoadException.LoadFailure(e)
                )
            }

        val combinedList = currentList.combineDiaryLists(loadedDiaryList)

        val resultList =
            try {
                updateDiaryListFooter(combinedList, searchWord)
            } catch (e: WordSearchListFooterUpdateException) {
                Log.e(logTag, "${logMsg}失敗_フッター更新処理エラー", e)
                return UseCaseResult.Failure(
                    WordSearchResultListAdditionLoadException.FooterUpdateFailure(e)
                )
            }

        Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${resultList.countDiaries()})")
        return UseCaseResult.Success(resultList)
    }

    /**
     * 指定されたオフセットから追加のワード検索結果リストを読み込む。
     *
     * @param loadOffset 読み込みを開始するオフセット（既に読み込まれている検索結果の数）。
     * @param searchWord 検索ワード。
     * @return 読み込まれたワード検索結果のリスト。
     * @throws WordSearchResultListLoadException ワード検索結果の読込に失敗した場合。
     */
    private suspend fun loadDiaryList(
        loadOffset: Int,
        searchWord: String
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        val result =
            loadWordSearchResultListUseCase(
                NUM_LOAD_ITEMS,
                loadOffset,
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
     * @param searchWord 検索ワード。（フッターの内容決定に使用）
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
