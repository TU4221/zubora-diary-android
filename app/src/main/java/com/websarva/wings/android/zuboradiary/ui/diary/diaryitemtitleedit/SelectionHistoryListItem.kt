package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItemEntity;

import java.time.LocalDateTime;
import java.util.Objects;

class SelectionHistoryListItem {

    private final String title;
    private final LocalDateTime log;

    SelectionHistoryListItem(DiaryItemTitleSelectionHistoryItemEntity item) {
        Objects.requireNonNull(item);

        title = item.getTitle();
        log = LocalDateTime.parse(item.getLog());
    }


    @NonNull
    String getTitle() {
        return title;
    }

    @NonNull
    LocalDateTime getLog() {
        return log;
    }
}
