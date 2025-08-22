package com.websarva.wings.android.zuboradiary.ui.model.list.diary

internal class DiaryYearMonthListUi<T: DiaryDayListItemUi> {

    val itemList: List<DiaryYearMonthListItemUi<T>>

    val isNotEmpty get() = itemList.isNotEmpty()

    constructor(itemList: List<DiaryYearMonthListItemUi<T>>) {
        this.itemList = itemList
    }

    /**
     * true:日記なしメッセージのみのリスト作成<br></br>
     * false:ProgressIndicatorのみのリスト作成
     */
    constructor(needsNoDiaryMessage: Boolean) {
        val emptyList: List<DiaryYearMonthListItemUi<T>> = ArrayList()

        this.itemList = emptyList + DiaryYearMonthListItemUi.ProgressIndicator()
    }

    constructor() {
        this.itemList = ArrayList()
    }

    fun countDiaries(): Int {
        var count = 0
        for (item in itemList) {
            if (item is DiaryYearMonthListItemUi.Diary) {
                count += item.diaryDayList.countDiaries()
            }
        }
        return count
    }
}
