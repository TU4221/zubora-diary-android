package com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.database.DiaryListItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem

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

    override fun areContentsTheSame(item: DiaryDayListBaseItem): Boolean {
        if (this === item) return true
        if (item !is DiaryDayListItem) return false

        return title == item.title && picturePath == item.picturePath
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiaryDayListItem) return false
        if (!super.equals(other)) return false

        return title == other.title && picturePath == other.picturePath
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (picturePath?.hashCode() ?: 0)
        return result
    }
}
