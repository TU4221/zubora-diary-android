package com.websarva.wings.android.zuboradiary.domain.model.list.diary

/**
 * 年月ごとの日記リストアイテムを保持する汎用的なリストクラス。
 *
 * このクラスは、[DiaryYearMonthListItem] を継承する任意の型のアイテムのリストをカプセル化する。
 * リストの末尾が [DiaryYearMonthListItem.Diary] でないことを保証し、
 * 基本、末尾は [DiaryYearMonthListItem.ProgressIndicator]となる。
 * フッター(リスト最終アイテム)を「日記なし」メッセージに置き換える機能を提供する。
 *
 * @param T [DiaryDayListItem] を実装するアイテムの型。
 * @property itemList [DiaryYearMonthListItem<T>] 型のアイテムのリスト。デフォルトは空のリスト。
 * @throws IllegalArgumentException [itemList] が空でなく、かつ最後のアイテムが [DiaryYearMonthListItem.Diary] の場合。
 */
internal data class DiaryYearMonthList<T: DiaryDayListItem>(
    val itemList: List<DiaryYearMonthListItem<T>> = emptyList()
) {

    /**
     * リストにアイテムが1つ以上含まれているかどうかを示す。
     *
     * @return アイテムリストが空でない場合は `true`、空の場合は `false`。
     */
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

    /**
     * アイテムリストの末尾をプログレスインジケータにした新しいリストを返す。
     *
     * @param itemList 元のアイテムリスト。
     * @return 末尾に [DiaryYearMonthListItem.ProgressIndicator] が追加された新しいリスト。
     */
    private fun applyProgressIndicatorAsLastItem(itemList: List<DiaryYearMonthListItem<T>>)
        : List<DiaryYearMonthListItem<T>> {
        return itemList.filterIsInstance<DiaryYearMonthListItem.Diary<T>>() +
                DiaryYearMonthListItem.ProgressIndicator()
    }

    /**
     * アイテムリストの末尾を「日記なし」メッセージアイテムにした新しいリストを返す。
     *
     * @param itemList 元のアイテムリスト。
     * @return 末尾に [DiaryYearMonthListItem.NoDiaryMessage] が追加された新しいリスト。
     */
    private fun applyNoDiaryMessageAsLastItem(itemList: List<DiaryYearMonthListItem<T>>)
        : List<DiaryYearMonthListItem<T>> {
        return itemList.filterIsInstance<DiaryYearMonthListItem.Diary<T>>() +
                DiaryYearMonthListItem.NoDiaryMessage()
    }

    /**
     * リストに含まれる日記の総数をカウントする。
     *
     * @return 日記の総数。
     */
    fun countDiaries(): Int {
        var count = 0
        for (item in itemList) {
            if (item is DiaryYearMonthListItem.Diary) {
                count += item.diaryDayList.countDiaries()
            }
        }
        return count
    }

    /**
     * このリストと指定された別の [DiaryYearMonthList] を結合し、新しい [DiaryYearMonthList] を返す。
     *
     * 結合処理では、年月が同じアイテムがあれば、その中の日記リスト ([DiaryDayList]) を結合する。
     * 結合後のリストの末尾にはプログレスインジケータが追加される。
     *
     * @param additionList このリストに結合する [DiaryYearMonthList]。空であってはならない。
     * @return ２つのリストを結合し、末尾にプログレスインジケータが追加された新しい [DiaryYearMonthList]。
     * @throws IllegalArgumentException [additionList] が空の場合。
     */
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
        return DiaryYearMonthList(
            applyProgressIndicatorAsLastItem(resultItemList)
        )
    }

    /**
     * リストのフッター（通常はプログレスインジケータ）を「日記なし」メッセージに置き換えた新しい [DiaryYearMonthList] を返す。
     *
     * この操作は、リストが空でない場合にのみ有効。
     *
     * @return フッターが「日記なし」メッセージに置き換えられた新しい [DiaryYearMonthList]。
     *         元のリストが空の場合は、元のリストをそのまま返す。
     */
    fun replaceFooterWithNoDiaryMessage(): DiaryYearMonthList<T> {
        if (itemList.isEmpty()) return this

        return DiaryYearMonthList(
            applyNoDiaryMessageAsLastItem(itemList)
        )
    }
}
