package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_item_title_selection_history")
internal data class DiaryItemTitleSelectionHistoryEntity (
    @PrimaryKey
    val title: String,
    val log: String
)
