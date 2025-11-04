package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiaryDayListUi<T: DiaryDayListItemUi>(
    val itemList: List<T>
) : Parcelable {

    val isNotEmpty get() = itemList.isNotEmpty()

    init {
        require(itemList.isNotEmpty())
    }

    fun countDiaries(): Int {
        return itemList.size
    }
}
