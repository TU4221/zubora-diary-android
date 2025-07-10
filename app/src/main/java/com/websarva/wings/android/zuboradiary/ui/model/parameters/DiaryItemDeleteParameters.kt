package com.websarva.wings.android.zuboradiary.ui.model.parameters

import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import java.io.Serializable

internal data class DiaryItemDeleteParameters(
    val itemNumber: ItemNumber
) : Serializable
