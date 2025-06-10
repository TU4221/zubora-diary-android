package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class CheckDiaryExistsUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(date: LocalDate): UseCaseResult<Boolean> {
        val logMsg = "日記既存確認_"
        Log.i(logTag, "${logMsg}開始")
        return try {
            val exists = diaryRepository.existsDiary(date)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(exists)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗", e)
            UseCaseResult.Error(e)
        }
    }
}
