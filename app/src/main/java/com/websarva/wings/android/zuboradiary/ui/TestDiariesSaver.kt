package com.websarva.wings.android.zuboradiary.ui;

import android.util.Log;

import com.websarva.wings.android.zuboradiary.ui.diary.diaryedit.DiaryEditViewModel;

import java.time.LocalDate;

// TODO:最終的に削除
public class TestDiariesSaver {
    private final DiaryEditViewModel diaryEditViewModel;

    public TestDiariesSaver(DiaryEditViewModel diaryEditViewModel) {
        this.diaryEditViewModel = diaryEditViewModel;
    }

    public void save(long number) {
        LocalDate startDate = diaryEditViewModel.getDate().getValue();
        if (startDate != null) {
            Log.d("20240823", "save");
            for (long i = 0; i < number; i++) {
                LocalDate savingDate = startDate.minusDays(i);
                diaryEditViewModel.updateDate(savingDate);
                diaryEditViewModel.saveDiary();
            }
        }
    }
}
