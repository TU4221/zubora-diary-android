package com.websarva.wings.android.zuboradiary.ui.model.result

import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import java.io.Serializable

internal data class ItemTitleEditResult(
    val itemNumber: ItemNumber,
    val title: String,
) : Serializable
