package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.ColumnInfo

internal data class DiaryListItemData(
    var date: String,
    var title: String,
    @ColumnInfo(name = "image_uri")
    var imageUriString: String?,
)
