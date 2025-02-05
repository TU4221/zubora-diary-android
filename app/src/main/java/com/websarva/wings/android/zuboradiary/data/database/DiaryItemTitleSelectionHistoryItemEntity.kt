package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_item_title_selection_history")
class DiaryItemTitleSelectionHistoryItemEntity {
    @PrimaryKey
    var title: String = ""
    var log: String = ""
}
