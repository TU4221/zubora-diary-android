package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.error.DoesDiaryExistError
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class DoesDiaryExistUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(date: LocalDate): UseCaseResult<Boolean, DoesDiaryExistError> {
        val logMsg = "日記既存確認_"
        Log.i(logTag, "${logMsg}開始")
        return try {
            val exists = diaryRepository.existsDiary(date)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(exists)
        } catch (e: Exception) {
            val error = DoesDiaryExistError.CheckDiaryExistence(e)
            Log.e(logTag, "${logMsg}失敗", error)
            UseCaseResult.Error(error)
        }
    }
}
