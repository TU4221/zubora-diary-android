package com.websarva.wings.android.zuboradiary.ui.model.diary.item

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiaryItemTitleSelectionUi(
    val itemNumber: Int,
    val id: String?,
    val title: String,
) : Parcelable
