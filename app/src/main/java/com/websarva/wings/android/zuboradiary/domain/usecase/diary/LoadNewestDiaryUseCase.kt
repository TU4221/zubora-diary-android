package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class LoadNewestDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): DefaultUseCaseResult<Diary?> {
        val logMsg = "最新日記読込_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val diary = diaryRepository.loadNewestDiary()
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(diary)
        } catch (e: DiaryLoadFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }
}
