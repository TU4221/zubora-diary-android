package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryExistenceCheckFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class DoesDiaryExistUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(date: LocalDate): DefaultUseCaseResult<Boolean> {
        val logMsg = "日記既存確認_"
        Log.i(logTag, "${logMsg}開始")
        return try {
            val exists = diaryRepository.existsDiary(date)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(exists)
        } catch (e: DiaryExistenceCheckFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            UseCaseResult.Failure(e)
        }
    }
}
