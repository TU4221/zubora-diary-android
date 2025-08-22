package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class UpdateDiaryListFooterUseCase(
    private val checkUnloadedDiariesExistUseCase: CheckUnloadedDiariesExistUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        list: DiaryYearMonthList<DiaryDayListItem.Standard>,
        startDate: LocalDate?
    ): DefaultUseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>> {
        val logMsg = "日記リストフッター更新_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val numLoadedDiaries = list.countDiaries()
            val resultList =
                when (val result = checkUnloadedDiariesExistUseCase(numLoadedDiaries, startDate)) {
                    is UseCaseResult.Success -> {
                        if (result.value) {
                            list
                        } else {
                            list.replaceFooterWithNoDiaryMessage()
                        }
                    }
                    is UseCaseResult.Failure -> throw result.exception
                }

            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(resultList)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }
}
