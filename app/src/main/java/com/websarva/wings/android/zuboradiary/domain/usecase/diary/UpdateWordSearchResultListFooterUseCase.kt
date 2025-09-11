package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.UnloadedWordSearchResultsExistCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * ワード検索結果リストのフッターを更新するユースケース。
 *
 * 未読込の検索結果が存在するかどうかを確認し、存在しない場合はリストのフッターを
 * プログレスインディケーターから「これ以上の検索結果はありません」というメッセージに置き換える。
 *
 * @property checkUnloadedWordSearchResultsExistUseCase 未読込のワード検索結果が存在するかどうかを確認するユースケース。
 */
internal class UpdateWordSearchResultListFooterUseCase(
    private val checkUnloadedWordSearchResultsExistUseCase: CheckUnloadedWordSearchResultsExistUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "ワード検索結果リストフッター更新_"

    /**
     * ユースケースを実行し、必要に応じてフッターが更新されたワード検索結果リストを返す。
     *
     * @param list フッターを更新する対象のワード検索結果リスト。
     * @param searchWord 検索キーワード。未読込の検索結果存在確認に使用される。
     * @return 処理に成功した場合は [UseCaseResult.Success] に更新されたワード検索結果リスト、
     *   または元のリスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [WordSearchListFooterUpdateException] を格納して返す。
     */
    suspend operator fun invoke(
        list: DiaryYearMonthList<DiaryDayListItem.WordSearchResult>,
        searchWord: String
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>, WordSearchListFooterUpdateException> {
        Log.i(logTag, "${logMsg}開始 (リスト件数: ${list.countDiaries()}, 検索ワード: \"$searchWord\")")

        try {
            val numLoadedDiaries = list.countDiaries()
            val resultList =
                when (val result = checkUnloadedWordSearchResultsExistUseCase(searchWord, numLoadedDiaries)) {
                    is UseCaseResult.Success -> {
                        if (result.value) {
                            // 未読込の検索結果が存在する場合、リストは変更しない
                            Log.i(logTag, "${logMsg}完了_未読込の検索結果あり (リスト変更なし)")
                            list
                        } else {
                            // 未読込の検索結果が存在しない場合、フッターをメッセージに置き換える
                            Log.i(logTag, "${logMsg}完了_未読込の検索結果なし (フッターをメッセージに置換)")
                            list.replaceFooterWithNoDiaryMessage()
                        }
                    }
                    is UseCaseResult.Failure -> throw result.exception
                }

            return UseCaseResult.Success(resultList)
        } catch (e: UnloadedWordSearchResultsExistCheckException) {
            Log.e(logTag, "${logMsg}失敗_未読込の検索結果存在確認エラー", e)
            return UseCaseResult.Failure(
                WordSearchListFooterUpdateException.UpdateFailure(e)
            )
        }
    }
}
