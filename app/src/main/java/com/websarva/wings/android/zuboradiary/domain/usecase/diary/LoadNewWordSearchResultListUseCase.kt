package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.NUM_LOAD_ITEMS
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListNewLoadException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 新規にワード検索結果リストを読み込み、フッターを更新するユースケース。
 *
 * 新しい検索キーワードで検索を実行した際に、最初の検索結果を取得するために使用される。
 *
 * @property loadWordSearchResultListUseCase ワード検索結果リストを読み込むためのユースケース。
 * @property updateWordSearchResultListFooterUseCase ワード検索結果リストのフッターを更新するためのユースケース。
 */
internal class LoadNewWordSearchResultListUseCase(
    private val loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
    private val updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "新規ワード検索結果リスト読込_"

    /**
     * ユースケースを実行し、新規にワード検索結果リストを読み込み、フッターを更新したリストを返す。
     *
     * @param searchWord 検索ワード。
     * @return 処理に成功した場合は [UseCaseResult.Success] に新しい検索結果リスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [WordSearchResultListNewLoadException] を格納して返す。
     */
    suspend operator fun invoke(
        searchWord: String
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>, WordSearchResultListNewLoadException> {
        Log.i(logTag, "${logMsg}開始 (検索ワード: \"$searchWord\")")

        return try {
            val loadedDiaryList = loadDiaryList(searchWord)
            val resultList = updateDiaryListFooter(loadedDiaryList, searchWord)
            Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${resultList.countDiaries()})")
            UseCaseResult.Success(resultList)
        } catch (e: WordSearchResultListLoadException) {
            when (e) {
                is WordSearchResultListLoadException.LoadFailure -> {
                    Log.e(logTag, "${logMsg}失敗_新規ワード検索結果読込エラー", e)
                    UseCaseResult.Failure(
                        WordSearchResultListNewLoadException.LoadFailure(e)
                    )
                }
                is WordSearchResultListLoadException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        WordSearchResultListNewLoadException.Unknown(e)
                    )
                }
            }
        } catch (e: WordSearchListFooterUpdateException) {
            when (e) {
                is WordSearchListFooterUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_フッター更新エラー", e)
                    UseCaseResult.Failure(
                        WordSearchResultListNewLoadException.LoadFailure(e)
                    )
                }
                is WordSearchListFooterUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        WordSearchResultListNewLoadException.Unknown(e)
                    )
                }
            }
        }
    }

    /**
     * 新規のワード検索結果リストを読み込む。
     *
     * @param searchWord 検索ワード。
     * @return 読み込まれたワード検索結果のリスト。
     * @throws WordSearchResultListLoadException ワード検索結果の読込に失敗した場合。
     */
    private suspend fun loadDiaryList(
        searchWord: String
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        val result =
            loadWordSearchResultListUseCase(
                NUM_LOAD_ITEMS,
                0, // 新規読み込みのためオフセットは0
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

