package com.websarva.wings.android.zuboradiary.domain.model

import java.time.LocalDate

internal data class DiaryListItem(
    val date: LocalDate,
    val title: String,
    val imageUriString: String
)
