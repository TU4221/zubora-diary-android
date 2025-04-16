package com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult

import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import java.time.YearMonth

internal class WordSearchResultYearMonthList {

    val itemList: List<WordSearchResultYearMonthListItem>

    constructor(
        wordSearchResultDayList: WordSearchResultDayList,
        needsNoDiaryMessage: Boolean
    ) {
        require(wordSearchResultDayList.itemList.isNotEmpty())

        val itemList =
            createWordSearchResultYearMonthListItem(wordSearchResultDayList)
        this.itemList = addLastItem(itemList, needsNoDiaryMessage)
    }

    constructor(
        itemList: List<WordSearchResultYearMonthListItem>,
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
        val emptyList: List<WordSearchResultYearMonthListItem> = ArrayList()
        this.itemList = addLastItem(emptyList, needsNoDiaryMessage)
    }

    constructor() {
        this.itemList = ArrayList()
    }

    private fun createWordSearchResultYearMonthListItem(
        wordSearchResultDayList: WordSearchResultDayList
    ): List<WordSearchResultYearMonthListItem> {
        require(wordSearchResultDayList.itemList.isNotEmpty())

        var sortingDayItemList: MutableList<WordSearchResultDayListItem> = ArrayList()
        val resultYearMonthListItemList: MutableList<WordSearchResultYearMonthListItem> = ArrayList()
        var resultYearMonthListItem: WordSearchResultYearMonthListItem
        var sortingYearMonth: YearMonth? = null

        // DayListItemを対象YearMonthListItem毎に仕分け
        val resultDayListItemList = wordSearchResultDayList.itemList
        for (day in resultDayListItemList) {
            val date = day.date
            val yearMonth = YearMonth.of(date.year, date.month)

            if (sortingYearMonth != null && yearMonth != sortingYearMonth) {
                val sortedWordSearchResultDayList = WordSearchResultDayList(sortingDayItemList)
                resultYearMonthListItem =
                    WordSearchResultYearMonthListItem(
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
        val sortedWordSearchResultDayList = WordSearchResultDayList(sortingDayItemList)
        resultYearMonthListItem =
            WordSearchResultYearMonthListItem(
                checkNotNull(sortingYearMonth),
                sortedWordSearchResultDayList
            )
        resultYearMonthListItemList.add(resultYearMonthListItem)

        return resultYearMonthListItemList
    }

    private fun addLastItem(
        itemList: List<WordSearchResultYearMonthListItem>,
        needsNoDiaryMessage: Boolean
    ): List<WordSearchResultYearMonthListItem> {
        val mutableItemList = itemList.toMutableList()
        mutableItemList.removeIf(DiaryYearMonthListBaseItem::isNotDiaryViewType)

        return if (needsNoDiaryMessage) {
            addLastItemNoDiaryMessage(mutableItemList)
        } else {
            addLastItemProgressIndicator(mutableItemList)
        }
    }

    private fun addLastItemProgressIndicator(
        itemList: List<WordSearchResultYearMonthListItem>
    ): List<WordSearchResultYearMonthListItem> {

        return itemList +
                WordSearchResultYearMonthListItem(
                    DiaryYearMonthListBaseAdapter.ViewType.PROGRESS_INDICATOR
                )
    }

    private fun addLastItemNoDiaryMessage(
        itemList: List<WordSearchResultYearMonthListItem>
    ): List<WordSearchResultYearMonthListItem> {
        return itemList +
                WordSearchResultYearMonthListItem(
                    DiaryYearMonthListBaseAdapter.ViewType.NO_DIARY_MESSAGE
                )
    }

    fun countDiaries(): Int {
        var count = 0
        for (item in itemList) {
            if (item.viewType == DiaryYearMonthListBaseAdapter.ViewType.DIARY) {
                count += item.wordSearchResultDayList.countDiaries()
            }
        }
        return count
    }

    fun combineDiaryLists(
        additionList: WordSearchResultYearMonthList, needsNoDiaryMessage: Boolean
    ): WordSearchResultYearMonthList {
        require(additionList.itemList.isNotEmpty())

        val originalItemList: MutableList<WordSearchResultYearMonthListItem> =
            itemList.toMutableList()
        val additionItemList: MutableList<WordSearchResultYearMonthListItem> =
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
            val originalLastWordSearchResultDayList =
                originalItemList[originalListLastItemPosition].wordSearchResultDayList
            val additionWordSearchResultDayList = additionListFirstItem.wordSearchResultDayList
            val combinedWordSearchResultDayList =
                originalLastWordSearchResultDayList.combineDiaryDayLists(
                    additionWordSearchResultDayList
                )
            val combinedResultYearMonthListItem = WordSearchResultYearMonthListItem(
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
