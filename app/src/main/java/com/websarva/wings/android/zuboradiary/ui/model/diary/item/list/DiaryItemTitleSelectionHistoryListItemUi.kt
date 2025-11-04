package com.websarva.wings.android.zuboradiary.ui.model.diary.item.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiaryItemTitleSelectionHistoryListItemUi(
    val id: String,
    val title: String
) : Parcelable
