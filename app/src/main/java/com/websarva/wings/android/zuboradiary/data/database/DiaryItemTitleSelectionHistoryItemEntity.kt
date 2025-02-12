package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_item_title_selection_history")
data class DiaryItemTitleSelectionHistoryItemEntity (
    @PrimaryKey
    val title: String,

    val log: String
)
