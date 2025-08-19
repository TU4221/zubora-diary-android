package com.websarva.wings.android.zuboradiary.ui.model.list.diary.diary

import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListBaseItem
import java.time.YearMonth

internal class DiaryYearMonthList {

    val itemList: List<DiaryYearMonthListItem>

    val isEmpty get() = itemList.isEmpty()
    val isNotEmpty get() = itemList.isNotEmpty()

    constructor(diaryDayList: DiaryDayList, needsNoDiaryMessage: Boolean) {
        require(diaryDayList.isNotEmpty)

        val itemList = createDiaryYearMonthListItem(diaryDayList)
        this.itemList = addLastItem(itemList, needsNoDiaryMessage)
    }

    private constructor(itemList: List<DiaryYearMonthListItem>, needsNoDiaryMessage: Boolean) {
        require(itemList.isNotEmpty())

        this.itemList = addLastItem(itemList, needsNoDiaryMessage)
    }

    /**
     * true:日記なしメッセージのみのリスト作成<br></br>
     * false:ProgressIndicatorのみのリスト作成
     */
    constructor(needsNoDiaryMessage: Boolean) {
        val emptyList: List<DiaryYearMonthListItem> = ArrayList()

        this.itemList = addLastItem(emptyList, needsNoDiaryMessage)
    }

    constructor() {
        this.itemList = ArrayList()
    }

    private fun createDiaryYearMonthListItem(diaryDayList: DiaryDayList): List<DiaryYearMonthListItem> {
        require(diaryDayList.isNotEmpty)

        var sortingDayItemList: MutableList<DiaryDayListItem> = ArrayList()
        val diaryYearMonthListItemList: MutableList<DiaryYearMonthListItem> = ArrayList()
        var diaryYearMonthListItem: DiaryYearMonthListItem
        var sortingYearMonth: YearMonth? = null

        val diaryDayListItemList = diaryDayList.itemList
        for (day in diaryDayListItemList) {
            val date = day.date
            val yearMonth = YearMonth.of(date.year, date.month)

            if (sortingYearMonth != null && yearMonth != sortingYearMonth) {
                val sortedDiaryDayList = DiaryDayList(sortingDayItemList)
                diaryYearMonthListItem =
                    DiaryYearMonthListItem(sortingYearMonth, sortedDiaryDayList)
                diaryYearMonthListItemList.add(diaryYearMonthListItem)
                sortingDayItemList = ArrayList()
            }
            sortingDayItemList.add(day)
            sortingYearMonth = yearMonth
        }

        val sortedDiaryDayList = DiaryDayList(sortingDayItemList)
        diaryYearMonthListItem =
            DiaryYearMonthListItem(checkNotNull(sortingYearMonth) , sortedDiaryDayList)
        diaryYearMonthListItemList.add(diaryYearMonthListItem)
        return diaryYearMonthListItemList
    }

    private fun addLastItem(
        itemList: List<DiaryYearMonthListItem>,
        needsNoDiaryMessage: Boolean
    ) : List<DiaryYearMonthListItem> {
        val mutableItemList = itemList.toMutableList()
        mutableItemList.removeIf(DiaryYearMonthListBaseItem::isNotDiaryViewType)
        return if (needsNoDiaryMessage) {
            addLastItemNoDiaryMessage(mutableItemList)
        } else {
            addLastItemProgressIndicator(mutableItemList)
        }
    }

    private fun addLastItemProgressIndicator(itemList: List<DiaryYearMonthListItem>)
        : List<DiaryYearMonthListItem> {
        return itemList +
                DiaryYearMonthListItem(DiaryYearMonthListBaseAdapter.ViewType.PROGRESS_INDICATOR)
    }

    private fun addLastItemNoDiaryMessage(itemList: List<DiaryYearMonthListItem>)
        : List<DiaryYearMonthListItem> {
        return itemList +
                DiaryYearMonthListItem(DiaryYearMonthListBaseAdapter.ViewType.NO_DIARY_MESSAGE)
    }

    fun countDiaries(): Int {
        var count = 0
        for (item in itemList) {
            if (item.viewType == DiaryYearMonthListBaseAdapter.ViewType.DIARY) {
                count += item.diaryDayList.countDiaries()
            }
        }
        return count
    }

    fun combineDiaryLists(
        additionList: DiaryYearMonthList, needsNoDiaryMessage: Boolean
    ): DiaryYearMonthList {
        require(additionList.isNotEmpty)

        val originalItemList: MutableList<DiaryYearMonthListItem> =
            itemList.toMutableList()
        val additionItemList: MutableList<DiaryYearMonthListItem> =
            additionList.itemList.toMutableList()

        // List最終アイテム(日記以外
        originalItemList.removeIf(DiaryYearMonthListBaseItem::isNotDiaryViewType)
        additionItemList.removeIf(DiaryYearMonthListBaseItem::isNotDiaryViewType)

        // 元リスト最終アイテムの年月取得
        val originalListLastItemPosition = originalItemList.size - 1
        val originalListLastItem = originalItemList[originalListLastItemPosition]
        val originalListLastItemYearMonth = originalListLastItem.yearMonth

        // 追加リスト先頭アイテムの年月取得
        val additionListFirstItem = additionList.itemList[0]
        val additionListFirstItemYearMonth = additionListFirstItem.yearMonth

        // 元リストに追加リストの年月が含まれていたらアイテムを足し込む
        if (originalListLastItemYearMonth == additionListFirstItemYearMonth) {
            val originalLastDiaryDayList =
                originalItemList[originalListLastItemPosition].diaryDayList
            val additionDiaryDayList = additionListFirstItem.diaryDayList
            val combinedDiaryDayList =
                originalLastDiaryDayList.combineDiaryDayLists(additionDiaryDayList)
            val combinedDiaryYearMonthListItem =
                DiaryYearMonthListItem(originalListLastItemYearMonth, combinedDiaryDayList)
            originalItemList.removeAt(originalListLastItemPosition)
            originalItemList.add(combinedDiaryYearMonthListItem)
            additionItemList.removeAt(0)
        }

        val resultItemList = originalItemList + additionItemList
        return DiaryYearMonthList(resultItemList, needsNoDiaryMessage)
    }
}
