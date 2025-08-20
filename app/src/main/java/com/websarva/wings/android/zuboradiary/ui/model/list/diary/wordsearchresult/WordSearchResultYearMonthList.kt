package com.websarva.wings.android.zuboradiary.ui.model.list.diary.wordsearchresult

import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayList
import java.time.YearMonth

internal class WordSearchResultYearMonthList {

    val itemList: List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>>

    val isEmpty get() = itemList.isEmpty()
    val isNotEmpty get() = itemList.isNotEmpty()

    constructor(
        wordSearchResultDayList: DiaryDayList<DiaryDayListItem.WordSearchResult>,
        needsNoDiaryMessage: Boolean
    ) {
        require(wordSearchResultDayList.isNotEmpty)

        val itemList =
            createWordSearchResultYearMonthListItem(wordSearchResultDayList)
        this.itemList = addLastItem(itemList, needsNoDiaryMessage)
    }

    private constructor(
        itemList: List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>>,
        needsNoDiaryMessage: Boolean
    ) {
        require(itemList.isNotEmpty())

        this.itemList = addLastItem(itemList, needsNoDiaryMessage)
    }

    /**
     * true:日記なしメッセージのみのリスト作成<br></br>
     * false:ProgressIndicatorのみのリスト作成
     */
    constructor(needsNoDiaryMessage: Boolean) {
        val emptyList: List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>> = ArrayList()
        this.itemList = addLastItem(emptyList, needsNoDiaryMessage)
    }

    constructor() {
        this.itemList = ArrayList()
    }

    private fun createWordSearchResultYearMonthListItem(
        wordSearchResultDayList: DiaryDayList<DiaryDayListItem.WordSearchResult>
    ): List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>> {
        require(wordSearchResultDayList.isNotEmpty)

        var sortingDayItemList: MutableList<DiaryDayListItem.WordSearchResult> = ArrayList()
        val resultYearMonthListItemList: MutableList<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>> = ArrayList()
        var resultYearMonthListItem: DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>
        var sortingYearMonth: YearMonth? = null

        // DayListItemを対象YearMonthListItem毎に仕分け
        val resultDayListItemList = wordSearchResultDayList.itemList
        for (day in resultDayListItemList) {
            val date = day.date
            val yearMonth = YearMonth.of(date.year, date.month)

            if (sortingYearMonth != null && yearMonth != sortingYearMonth) {
                val sortedWordSearchResultDayList = DiaryDayList(sortingDayItemList)
                resultYearMonthListItem =
                    DiaryYearMonthListItem.Diary(
                        sortingYearMonth,
                        sortedWordSearchResultDayList
                    )
                resultYearMonthListItemList.add(resultYearMonthListItem)
                sortingDayItemList = ArrayList()
            }
            sortingDayItemList.add(day)
            sortingYearMonth = yearMonth
        }

        // 最後尾YearMonthListItemの追加処理
        val sortedWordSearchResultDayList = DiaryDayList(sortingDayItemList)
        resultYearMonthListItem =
            DiaryYearMonthListItem.Diary(
                checkNotNull(sortingYearMonth),
                sortedWordSearchResultDayList
            )
        resultYearMonthListItemList.add(resultYearMonthListItem)

        return resultYearMonthListItemList
    }

    private fun addLastItem(
        itemList: List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>>,
        needsNoDiaryMessage: Boolean
    ): List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>> {
        val mutableItemList =
            itemList.filterIsInstance<DiaryYearMonthListItem.Diary<DiaryDayListItem.WordSearchResult>>()

        return if (needsNoDiaryMessage) {
            addLastItemNoDiaryMessage(mutableItemList)
        } else {
            addLastItemProgressIndicator(mutableItemList)
        }
    }

    private fun addLastItemProgressIndicator(
        itemList: List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>>
    ): List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>> {
        return itemList + DiaryYearMonthListItem.ProgressIndicator()
    }

    private fun addLastItemNoDiaryMessage(
        itemList: List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>>
    ): List<DiaryYearMonthListItem<DiaryDayListItem.WordSearchResult>> {
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
        additionList: WordSearchResultYearMonthList, needsNoDiaryMessage: Boolean
    ): WordSearchResultYearMonthList {
        require(additionList.isNotEmpty)

        val originalItemList =
            itemList
                .filterIsInstance<DiaryYearMonthListItem.Diary<DiaryDayListItem.WordSearchResult>>()
                .toMutableList()
        val additionItemList =
            additionList.itemList
                .filterIsInstance<DiaryYearMonthListItem.Diary<DiaryDayListItem.WordSearchResult>>()
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
            val originalLastWordSearchResultDayList =
                originalItemList[originalListLastItemPosition].diaryDayList
            val additionWordSearchResultDayList = additionListFirstItem.diaryDayList
            val combinedWordSearchResultDayList =
                originalLastWordSearchResultDayList.combineDiaryDayLists(
                    additionWordSearchResultDayList
                )
            val combinedResultYearMonthListItem =
                DiaryYearMonthListItem.Diary(
                    originalListLastItemYearMonth,
                    combinedWordSearchResultDayList
                )
            originalItemList.removeAt(originalListLastItemPosition)
            originalItemList.add(combinedResultYearMonthListItem)
            additionItemList.removeAt(0)
        }

        val resultItemList = originalItemList + additionItemList
        return WordSearchResultYearMonthList(resultItemList, needsNoDiaryMessage)
    }
}
