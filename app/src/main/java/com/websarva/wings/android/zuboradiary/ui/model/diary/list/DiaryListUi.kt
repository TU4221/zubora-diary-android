package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.collections.isNotEmpty

@Parcelize
data class DiaryListUi<T: DiaryListItemContainerUi>(
    val itemList: List<DiaryListItemUi<T>> = emptyList()
) : Parcelable {

    val isEmpty get() = itemList.isEmpty()

    val isNotEmpty get() = itemList.isNotEmpty()

    fun countDiaries(): Int {
        return itemList.filterIsInstance<DiaryListItemUi.Diary<T>>().count()
    }
}
