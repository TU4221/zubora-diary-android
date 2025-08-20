package com.websarva.wings.android.zuboradiary.ui.model.list.diary.diary

import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItem
import java.time.YearMonth

internal class DiaryYearMonthList {

    val itemList: List<DiaryYearMonthListItem<DiaryDayListItem.Standard>>

    val isEmpty get() = itemList.isEmpty()
    val isNotEmpty get() = itemList.isNotEmpty()

    constructor(diaryDayList: DiaryDayList<DiaryDayListItem.Standard>, needsNoDiaryMessage: Boolean) {
        require(diaryDayList.isNotEmpty)

        val itemList = createDiaryYearMonthListItem(diaryDayList)
        this.itemList = addLastItem(itemList, needsNoDiaryMessage)
    }

    private constructor(itemList: List<DiaryYearMonthListItem<DiaryDayListItem.Standard>>, needsNoDiaryMessage: Boolean) {
        require(itemList.isNotEmpty())

        this.itemList = addLastItem(itemList, needsNoDiaryMessage)
    }

    /**
     * true:日記なしメッセージのみのリスト作成<br></br>
     * false:ProgressIndicatorのみのリスト作成
     */
    constructor(needsNoDiaryMessage: Boolean) {
        val emptyList: List<DiaryYearMonthListItem<DiaryDayListItem.Standard>> = ArrayList()

        this.itemList = addLastItem(emptyList, needsNoDiaryMessage)
    }

    constructor() {
        this.itemList = ArrayList()
    }

    private fun createDiaryYearMonthListItem(diaryDayList: DiaryDayList<DiaryDayListItem.Standard>): List<DiaryYearMonthListItem<DiaryDayListItem.Standard>> {
        require(diaryDayList.isNotEmpty)

        var sortingDayItemList: MutableList<DiaryDayListItem.Standard> = ArrayList()
        val diaryYearMonthListItemList: MutableList<DiaryYearMonthListItem<DiaryDayListItem.Standard>> = ArrayList()
        var diaryYearMonthListItem: DiaryYearMonthListItem.Diary<DiaryDayListItem.Standard>
        var sortingYearMonth: YearMonth? = null

        val diaryDayListItemList = diaryDayList.itemList
        for (day in diaryDayListItemList) {
            val date = day.date
            val yearMonth = YearMonth.of(date.year, date.month)

            if (sortingYearMonth != null && yearMonth != sortingYearMonth) {
                val sortedDiaryDayList = DiaryDayList(sortingDayItemList)
                diaryYearMonthListItem =
                    DiaryYearMonthListItem.Diary(sortingYearMonth, sortedDiaryDayList)
                diaryYearMonthListItemList.add(diaryYearMonthListItem)
                sortingDayItemList = ArrayList()
            }
            sortingDayItemList.add(day)
            sortingYearMonth = yearMonth
        }

        val sortedDiaryDayList = DiaryDayList(sortingDayItemList)
        diaryYearMonthListItem =
            DiaryYearMonthListItem.Diary(checkNotNull(sortingYearMonth) , sortedDiaryDayList)
        diaryYearMonthListItemList.add(diaryYearMonthListItem)
        return diaryYearMonthListItemList
    }

    private fun addLastItem(
        itemList: List<DiaryYearMonthListItem<DiaryDayListItem.Standard>>,
        needsNoDiaryMessage: Boolean
    ) : List<DiaryYearMonthListItem<DiaryDayListItem.Standard>> {
        val mutableItemList =
            itemList.filterIsInstance<DiaryYearMonthListItem.Diary<DiaryDayListItem.Standard>>()
        return if (needsNoDiaryMessage) {
            addLastItemNoDiaryMessage(mutableItemList)
        } else {
            addLastItemProgressIndicator(mutableItemList)
        }
    }

    private fun addLastItemProgressIndicator(itemList: List<DiaryYearMonthListItem<DiaryDayListItem.Standard>>)
        : List<DiaryYearMonthListItem<DiaryDayListItem.Standard>> {
        return itemList + DiaryYearMonthListItem.ProgressIndicator()
    }

    private fun addLastItemNoDiaryMessage(itemList: List<DiaryYearMonthListItem<DiaryDayListItem.Standard>>)
        : List<DiaryYearMonthListItem<DiaryDayListItem.Standard>> {
        return itemList + DiaryYearMonthListItem.NoDiaryMessage()
    }

    fun countDiaries(): Int {
        var count = 0
        for (item in itemList) {
            if (item is DiaryYearMonthListItem.Diary) {
                count += item.diaryDayList.countDiaries()
            }
        }
        return count
    }

    fun combineDiaryLists(
        additionList: DiaryYearMonthList, needsNoDiaryMessage: Boolean
    ): DiaryYearMonthList {
        require(additionList.isNotEmpty)

        val originalItemList =
            itemList
                .filterIsInstance<DiaryYearMonthListItem.Diary<DiaryDayListItem.Standard>>()
                .toMutableList()
        val additionItemList =
            additionList.itemList
                .filterIsInstance<DiaryYearMonthListItem.Diary<DiaryDayListItem.Standard>>()
                .toMutableList()

        // 元リスト最終アイテムの年月取得
        val originalListLastItemPosition = originalItemList.size - 1
        val originalListLastItem = originalItemList[originalListLastItemPosition]
        val originalListLastItemYearMonth = originalListLastItem.yearMonth

        // 追加リスト先頭アイテムの年月取得
        val additionListFirstItem = additionItemList[0]
        val additionListFirstItemYearMonth = additionListFirstItem.yearMonth

        // 元リストに追加リストの年月が含まれていたらアイテムを足し込む
        if (originalListLastItemYearMonth == additionListFirstItemYearMonth) {
            val originalLastDiaryDayList =
                originalItemList[originalListLastItemPosition].diaryDayList
            val additionDiaryDayList = additionListFirstItem.diaryDayList
            val combinedDiaryDayList =
                originalLastDiaryDayList.combineDiaryDayLists(additionDiaryDayList)
            val combinedDiaryYearMonthListItem =
                DiaryYearMonthListItem.Diary(originalListLastItemYearMonth, combinedDiaryDayList)
            originalItemList.removeAt(originalListLastItemPosition)
            originalItemList.add(combinedDiaryYearMonthListItem)
            additionItemList.removeAt(0)
        }

        val resultItemList = originalItemList + additionItemList
        return DiaryYearMonthList(resultItemList, needsNoDiaryMessage)
    }
}
