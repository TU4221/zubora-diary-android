package com.websarva.wings.android.zuboradiary.ui.list.diarylist

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem
import com.websarva.wings.android.zuboradiary.ui.list.DiaryDayListBaseItem

internal class DiaryDayListItem(listItem: DiaryListItem) :
    DiaryDayListBaseItem(listItem) {

    val title: String = listItem.title
    val picturePath: Uri?

    init {
        val picturePath = listItem.picturePath
        if (picturePath.isEmpty()) {
            this.picturePath = null
        } else {
            this.picturePath = Uri.parse(picturePath)
        }
    }
}
