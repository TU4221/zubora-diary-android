package com.websarva.wings.android.zuboradiary.ui.model.list.diary.wordsearchresult

import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItem
import java.time.YearMonth

internal class WordSearchResultYearMonthList {

    val itemList: List<DiaryYearMonthListItem<WordSearchResultDayList>>

    val isEmpty get() = itemList.isEmpty()
    val isNotEmpty get() = itemList.isNotEmpty()

    constructor(
        wordSearchResultDayList: WordSearchResultDayList,
        needsNoDiaryMessage: Boolean
    ) {
        require(wordSearchResultDayList.isNotEmpty)

        val itemList =
            createWordSearchResultYearMonthListItem(wordSearchResultDayList)
        this.itemList = addLastItem(itemList, needsNoDiaryMessage)
    }

    private constructor(
        itemList: List<DiaryYearMonthListItem<WordSearchResultDayList>>,
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
        val emptyList: List<DiaryYearMonthListItem<WordSearchResultDayList>> = ArrayList()
        this.itemList = addLastItem(emptyList, needsNoDiaryMessage)
    }

    constructor() {
        this.itemList = ArrayList()
    }

    private fun createWordSearchResultYearMonthListItem(
        wordSearchResultDayList: WordSearchResultDayList
    ): List<DiaryYearMonthListItem<WordSearchResultDayList>> {
        require(wordSearchResultDayList.isNotEmpty)

        var sortingDayItemList: MutableList<DiaryDayListItem.WordSearchResult> = ArrayList()
        val resultYearMonthListItemList: MutableList<DiaryYearMonthListItem<WordSearchResultDayList>> = ArrayList()
        var resultYearMonthListItem: DiaryYearMonthListItem<WordSearchResultDayList>
        var sortingYearMonth: YearMonth? = null

        // DayListItemを対象YearMonthListItem毎に仕分け
        val resultDayListItemList = wordSearchResultDayList.itemList
        for (day in resultDayListItemList) {
            val date = day.date
            val yearMonth = YearMonth.of(date.year, date.month)

            if (sortingYearMonth != null && yearMonth != sortingYearMonth) {
                val sortedWordSearchResultDayList = WordSearchResultDayList(sortingDayItemList)
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
        val sortedWordSearchResultDayList = WordSearchResultDayList(sortingDayItemList)
        resultYearMonthListItem =
            DiaryYearMonthListItem.Diary(
                checkNotNull(sortingYearMonth),
                sortedWordSearchResultDayList
            )
        resultYearMonthListItemList.add(resultYearMonthListItem)

        return resultYearMonthListItemList
    }

    private fun addLastItem(
        itemList: List<DiaryYearMonthListItem<WordSearchResultDayList>>,
        needsNoDiaryMessage: Boolean
    ): List<DiaryYearMonthListItem<WordSearchResultDayList>> {
        val mutableItemList =
            itemList.filterIsInstance<DiaryYearMonthListItem.Diary<WordSearchResultDayList>>()

        return if (needsNoDiaryMessage) {
            addLastItemNoDiaryMessage(mutableItemList)
        } else {
            addLastItemProgressIndicator(mutableItemList)
        }
    }

    private fun addLastItemProgressIndicator(
        itemList: List<DiaryYearMonthListItem<WordSearchResultDayList>>
    ): List<DiaryYearMonthListItem<WordSearchResultDayList>> {
        return itemList + DiaryYearMonthListItem.ProgressIndicator()
    }

    private fun addLastItemNoDiaryMessage(
        itemList: List<DiaryYearMonthListItem<WordSearchResultDayList>>
    ): List<DiaryYearMonthListItem<WordSearchResultDayList>> {
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
                .filterIsInstance<DiaryYearMonthListItem.Diary<WordSearchResultDayList>>()
                .toMutableList()
        val additionItemList =
            additionList.itemList
                .filterIsInstance<DiaryYearMonthListItem.Diary<WordSearchResultDayList>>()
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
