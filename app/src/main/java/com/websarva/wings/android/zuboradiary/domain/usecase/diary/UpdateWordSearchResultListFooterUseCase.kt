package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
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
 * @property countWordSearchResultsUseCase 検索ワードに一致する日記の総数を取得するためのユースケース。
 */
internal class UpdateWordSearchResultListFooterUseCase(
    private val countWordSearchResultsUseCase: CountWordSearchResultsUseCase
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
            val unloadedResultsExist = checkUnloadedWordSearchResultsExist(searchWord, numLoadedDiaries)
            val replacedResultList =
                if (unloadedResultsExist) {
                    // 未読込の検索結果が存在する場合、リストは変更しない
                    Log.i(logTag, "${logMsg}完了_未読込の検索結果あり (リスト変更なし)")
                    list
                } else {
                    // 未読込の検索結果が存在しない場合、フッターをメッセージに置き換える
                    Log.i(logTag, "${logMsg}完了_未読込の検索結果なし (フッターをメッセージに置換)")
                    list.replaceFooterWithNoDiaryMessage()
                }
            return UseCaseResult.Success(replacedResultList)
        } catch (e: WordSearchListFooterUpdateException) {
            return UseCaseResult.Failure(e)
        }
    }

    /**
     * 指定された検索ワードに一致する未読み込みの日記が存在するかどうかを確認する。
     *
     * 読み込み済みの日記数と指定された検索ワードに一致する全日記数を比較し、未読み込みの日記が存在するかを判定する。
     *
     * @param searchWord 検索するキーワード。
     * @param numLoadedDiaries 現在UIに読み込まれている、この検索ワードに一致する日記の数。
     * @return 未読み込みの日記が存在すれば `true`、そうでなければ `false`。
     * @throws WordSearchListFooterUpdateException.UpdateFailure 検索ワードに一致する日記の総数の取得に失敗した場合。
     */
    private suspend fun checkUnloadedWordSearchResultsExist(
        searchWord: String,
        numLoadedDiaries: Int
    ): Boolean {
            return when (val result = countWordSearchResultsUseCase(searchWord)) {
                is UseCaseResult.Success -> {
                    val numExistingDiaries = result.value
                    if (numExistingDiaries <= 0) {
                        false
                    } else {
                        numLoadedDiaries < numExistingDiaries
                    }
                }

                is UseCaseResult.Failure -> {
                    Log.e(logTag, "${logMsg}失敗_未読込の検索結果存在確認エラー", result.exception)
                    throw WordSearchListFooterUpdateException.UpdateFailure(result.exception)
                }
            }
    }
}
