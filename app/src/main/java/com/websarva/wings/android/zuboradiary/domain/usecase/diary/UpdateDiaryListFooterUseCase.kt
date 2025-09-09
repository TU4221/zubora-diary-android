package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListFooterUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.UnloadedDiariesExistCheckException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 日記リストのフッターを更新するユースケース。
 *
 * 未読込の日記が存在するかどうかを確認し、存在しない場合はリストのフッターを
 * プログレスインディケーターから「これ以上の日記はありません」というメッセージに置き換える。
 *
 * @property checkUnloadedDiariesExistUseCase 未読込の日記が存在するかどうかを確認するユースケース。
 */
internal class UpdateDiaryListFooterUseCase(
    private val checkUnloadedDiariesExistUseCase: CheckUnloadedDiariesExistUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "日記リストフッター更新_"

    /**
     * ユースケースを実行し、必要に応じてフッターが更新された日記リストを返す。
     *
     * @param list フッターを更新する対象の日記リスト。
     * @param startDate 日記の読み込み開始日。未読込の日記存在確認に使用される。`null` の場合は全期間が対象。
     * @return フッターが更新された日記リスト、または元のリストを [UseCaseResult.Success] に格納して返す。
     *   未読込の日記存在確認処理でエラーが発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        list: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, DiaryListFooterUpdateException> {
        Log.i(logTag, "${logMsg}開始 (リスト件数: ${list.countDiaries()}, 開始日: ${startDate ?: "全期間"})")

        try {
            val numLoadedDiaries = list.countDiaries()
            val resultList =
                when (val result = checkUnloadedDiariesExistUseCase(numLoadedDiaries, startDate)) {
                    is UseCaseResult.Success -> {
                        if (result.value) {
                            Log.i(logTag, "${logMsg}完了_未読込の日記あり (リスト変更なし)")
                            list
                        } else {
                            Log.i(logTag, "${logMsg}完了_未読込の日記なし (フッターをメッセージに置換)")
                            list.replaceFooterWithNoDiaryMessage()
                        }
                    }
                    is UseCaseResult.Failure -> throw result.exception
                }
            return UseCaseResult.Success(resultList)
        } catch (e: UnloadedDiariesExistCheckException) {
            Log.e(logTag, "${logMsg}失敗_未読込の日記存在確認エラー", e)
            return UseCaseResult.Failure(
                DiaryListFooterUpdateException.UpdateFailure(e)
            )
        }
    }
}
