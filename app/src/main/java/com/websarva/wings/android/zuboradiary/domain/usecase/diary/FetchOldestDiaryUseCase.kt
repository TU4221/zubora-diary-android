package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.FetchDiaryFailedException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class FetchOldestDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): DefaultUseCaseResult<Diary?> {
        val logMsg = "最古日記読込_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val diary = diaryRepository.fetchOldestDiary()
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(diary)
        } catch (e: FetchDiaryFailedException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }
}
