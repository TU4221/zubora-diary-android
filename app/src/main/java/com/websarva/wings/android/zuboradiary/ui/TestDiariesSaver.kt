package com.websarva.wings.android.zuboradiary.ui

import com.websarva.wings.android.zuboradiary.ui.viewmodel.DiaryEditViewModel

// TODO:最終的に削除
internal class TestDiariesSaver(private val diaryEditViewModel: DiaryEditViewModel) {
    suspend fun save(number: Long) {
        val startDate = diaryEditViewModel.date.value
        if (startDate != null) {
            for (i in 0 until number) {
                val savingDate = startDate.minusDays(i)
                diaryEditViewModel.updateDate(savingDate)
                diaryEditViewModel.saveDiary()
            }
        }
    }
}
