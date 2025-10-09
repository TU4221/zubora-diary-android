package com.websarva.wings.android.zuboradiary.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class DiaryItemTitleSelection(
    val itemNumber: Int,
    val id: String?,
    val title: String,
) : Parcelable
