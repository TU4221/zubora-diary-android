package com.websarva.wings.android.zuboradiary.ui.model.diary.item.list

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiaryItemTitleSelectionHistoryListItemUi(
    override val id: String,
    val title: String
) : Parcelable, Identifiable
