package com.websarva.wings.android.zuboradiary.ui.model.diary.item

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class DiaryItemTitleSelectionHistoryUi(
    override val id: String,
    val title: String,
    val log: LocalDateTime
) : Parcelable, Identifiable
