package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryLoadFailureException
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class LoadDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        date: LocalDate
    ): DefaultUseCaseResult<Diary> {
        val logMsg = "日記取得_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val diary = diaryRepository.loadDiary(date)
            requireNotNull(diary)
            Log.e(logTag, "${logMsg}完了")
            return UseCaseResult.Success(diary)
        } catch (e: DiaryLoadFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }
}
