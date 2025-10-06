package com.websarva.wings.android.zuboradiary.ui.model

import java.io.Serializable

internal data class DiaryItemTitle(
    val itemNumber: Int,
    val id: String?,
    val title: String,
) : Serializable
