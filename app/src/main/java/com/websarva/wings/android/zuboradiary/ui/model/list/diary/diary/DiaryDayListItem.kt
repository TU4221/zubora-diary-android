package com.websarva.wings.android.zuboradiary.ui.model.list.diary.diary

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListBaseItem

internal class DiaryDayListItem(listItem: DiaryListItem) :
    DiaryDayListBaseItem(listItem.date) {

    val title: String = listItem.title
    val imageUri: Uri? = listItem.imageUriString?.let { Uri.parse(it) }

    override fun areContentsTheSame(item: DiaryDayListBaseItem): Boolean {
        if (this === item) return true
        if (item !is DiaryDayListItem) return false

        return title == item.title && imageUri == item.imageUri
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiaryDayListItem) return false
        if (!super.equals(other)) return false

        return title == other.title && imageUri == other.imageUri
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (imageUri?.hashCode() ?: 0)
        return result
    }
}
