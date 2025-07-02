package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.CountDiariesFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class CountDiariesUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        startDate: LocalDate? = null
    ): DefaultUseCaseResult<Int> {
        val logMsg = "日記総数取得_"
        Log.i(logTag, "${logMsg}開始")

        val numDiaries =
            try {
                if (startDate == null) {
                    diaryRepository.countDiaries()
                } else {
                    diaryRepository.countDiaries(
                        startDate
                    )
                }
            } catch (e: CountDiariesFailedException) {
                return UseCaseResult.Failure(e)
            }

        return UseCaseResult.Success(numDiaries)
    }
}
