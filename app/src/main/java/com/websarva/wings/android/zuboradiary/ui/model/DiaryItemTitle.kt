package com.websarva.wings.android.zuboradiary.ui.model

import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import java.io.Serializable

internal data class DiaryItemTitle(
    val itemNumber: ItemNumber,
    val title: String,
) : Serializable
