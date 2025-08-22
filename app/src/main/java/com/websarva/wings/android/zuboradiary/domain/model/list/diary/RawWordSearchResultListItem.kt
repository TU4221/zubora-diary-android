package com.websarva.wings.android.zuboradiary.domain.model.list.diary

import java.time.LocalDate

internal data class RawWordSearchResultListItem(
    val date: LocalDate,
    val title: String,
    val item1Title: String,
    val item1Comment: String,
    val item2Title: String?,
    val item2Comment: String?,
    val item3Title: String?,
    val item3Comment: String?,
    val item4Title: String?,
    val item4Comment: String?,
    val item5Title: String?,
    val item5Comment: String?
)
