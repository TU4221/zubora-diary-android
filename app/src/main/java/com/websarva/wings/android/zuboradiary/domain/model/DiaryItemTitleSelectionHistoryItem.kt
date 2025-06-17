package com.websarva.wings.android.zuboradiary.domain.model

import java.time.LocalDateTime

internal data class DiaryItemTitleSelectionHistoryItem (
    val title: String,
    val log: LocalDateTime
)
