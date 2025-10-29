package com.websarva.wings.android.zuboradiary.ui.model.diary.item.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class DiaryItemTitleSelectionHistoryListUi(
    val itemList: List<DiaryItemTitleSelectionHistoryListItemUi>
) : Parcelable {
    val isEmpty
        get() = itemList.isEmpty()
}
