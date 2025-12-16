package com.websarva.wings.android.zuboradiary.domain.usecase.diary.list

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.diary.SearchWord
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.WordSearchListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.WordSearchResultListAdditionLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.WordSearchResultListLoadException
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import javax.inject.Inject

/**
 * 既存のワード検索結果リストに追加の検索結果データを読み込み、結合してフッターを更新するユースケース。
 *
 * ワード検索結果リストの末尾までスクロールした際に、追加の検索結果を読み込むために使用される。
 *
 * @property loadWordSearchResultListUseCase ワード検索結果リストを読み込むためのユースケース。
 * @property updateWordSearchResultListFooterUseCase ワード検索結果リストのフッターを更新するためのユースケース。
 */
internal class LoadAdditionWordSearchResultListUseCase @Inject constructor(
    private val loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
    private val updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
) {

    private val logMsg = "追加ワード検索結果リスト読込_"

    /**
     * ユースケースを実行し、現在のリストに追加の検索結果を読み込み、フッターを更新した新しいリストを返す。
     *
     * @param currentList 現在表示されているワード検索結果のリスト。
     * @param searchWord 検索ワード。
     * @return 処理に成功した場合は [UseCaseResult.Success] に新しい検索結果リスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [WordSearchResultListAdditionLoadException] を格納して返す。
     */
    suspend operator fun invoke(
        currentList: DiaryYearMonthList<DiaryDayListItem.WordSearchResult>,
        searchWord: SearchWord
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>, WordSearchResultListAdditionLoadException> {
        Log.i(logTag, "${logMsg}開始 (現リスト件数: ${currentList.countDiaries()}, 検索ワード: \"$searchWord\")")

        return try {
            val loadedDiaryList =
                loadDiaryList(
                    currentList.countDiaries(),
                    searchWord
                )
            val combinedList = currentList.combineDiaryLists(loadedDiaryList)
            val resultList = updateDiaryListFooter(combinedList, searchWord)

            Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${resultList.countDiaries()})")
            UseCaseResult.Success(resultList)
        } catch (e: WordSearchResultListLoadException) {
            when (e) {
                is WordSearchResultListLoadException.LoadFailure -> {
                    Log.e(logTag, "${logMsg}失敗_追加ワード検索結果読込エラー", e)
                    UseCaseResult.Failure(
                        WordSearchResultListAdditionLoadException.LoadFailure(e)
                    )
                }
                is WordSearchResultListLoadException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        WordSearchResultListAdditionLoadException.Unknown(e)
                    )
                }
            }
        } catch (e: WordSearchListFooterUpdateException) {
            when (e) {
                is WordSearchListFooterUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_フッター更新エラー", e)
                    UseCaseResult.Failure(
                        WordSearchResultListAdditionLoadException.LoadFailure(e)
                    )
                }
                is WordSearchListFooterUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        WordSearchResultListAdditionLoadException.Unknown(e)
                    )
                }
            }
        }
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
        searchWord: SearchWord
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
        searchWord: SearchWord
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
