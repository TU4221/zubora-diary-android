package com.websarva.wings.android.zuboradiary.ui.diary.diaryitemtitleedit;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class SelectionHistoryList {
    private final List<SelectionHistoryListItem> selectionHistoryListItemList;

    SelectionHistoryList(List<SelectionHistoryListItem> itemList) {
        Objects.requireNonNull(itemList);
        itemList.stream().forEach(Objects::requireNonNull);

        selectionHistoryListItemList = Collections.unmodifiableList(itemList);
    }

    SelectionHistoryList() {
        selectionHistoryListItemList = Collections.unmodifiableList(new ArrayList<>());
    }

    @NonNull
    SelectionHistoryList deleteItem(int position) {
        List<SelectionHistoryListItem> result = new ArrayList<>(selectionHistoryListItemList);
        result.remove(position);
        return new SelectionHistoryList(result);
    }

    @NonNull
    List<SelectionHistoryListItem> getSelectionHistoryListItemList() {
        return selectionHistoryListItemList;
    }
}
