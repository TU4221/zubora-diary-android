package com.websarva.wings.android.zuboradiary.domain.model.list.diary

internal data class DiaryYearMonthList<T: DiaryDayListItem>(
    val itemList: List<DiaryYearMonthListItem<T>> = ArrayList()
) {

    val isNotEmpty get() = itemList.isNotEmpty()

    init {
        require(
            if (itemList.isEmpty()) {
                true
            } else {
                itemList.last() !is DiaryYearMonthListItem.Diary
            }
        )
    }

    private fun addLastItemProgressIndicator(itemList: List<DiaryYearMonthListItem<T>>)
        : List<DiaryYearMonthListItem<T>> {
        return itemList + DiaryYearMonthListItem.ProgressIndicator()
    }

    private fun addLastItemNoDiaryMessage(itemList: List<DiaryYearMonthListItem<T>>)
        : List<DiaryYearMonthListItem<T>> {
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
        additionList: DiaryYearMonthList<T>
    ): DiaryYearMonthList<T> {
        require(additionList.isNotEmpty)

        val originalItemList =
            itemList
                .filterIsInstance<DiaryYearMonthListItem.Diary<T>>()
                .toMutableList()
        val additionItemList =
            additionList.itemList
                .filterIsInstance<DiaryYearMonthListItem.Diary<T>>()
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
        return DiaryYearMonthList(addLastItemProgressIndicator(resultItemList))
    }

    fun replaceFooterWithNoDiaryMessage(): DiaryYearMonthList<T> {
        if (itemList.isEmpty()) return this

        return DiaryYearMonthList(
            addLastItemNoDiaryMessage(
                itemList.filterIsInstance<DiaryYearMonthListItem.Diary<T>>()
            )
        )
    }
}
