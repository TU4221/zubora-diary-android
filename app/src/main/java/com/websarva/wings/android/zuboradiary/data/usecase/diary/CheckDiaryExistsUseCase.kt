package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class CheckDiaryExistsUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(date: LocalDate): Boolean {
        return try {
            diaryRepository.existsDiary(date)
        } catch (e: Exception) {
            Log.e(createLogTag(), "日記既存確認_失敗", e)
            throw e
        }
    }
}
