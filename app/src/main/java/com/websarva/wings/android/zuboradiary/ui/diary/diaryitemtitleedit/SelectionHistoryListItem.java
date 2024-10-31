package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;

import java.time.LocalDateTime;
import java.util.Objects;

public class SelectionHistoryListItem {

    private final String title;
    private final LocalDateTime log;

    SelectionHistoryListItem(DiaryItemTitleSelectionHistoryItem item) {
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
