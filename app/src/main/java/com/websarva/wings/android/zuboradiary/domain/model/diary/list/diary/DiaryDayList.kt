package com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary

/**
 * 日記の日単位のリストアイテムを保持する汎用的なリストクラス。
 *
 * このクラスは、[DiaryDayListItem] を継承する任意の型のアイテムのリストをカプセル化する。
 * リストが空でないことを保証。
 *
 * @param T [DiaryDayListItem] を実装するアイテムの型。
 * @property itemList [T] 型のアイテムのリスト。
 * @throws IllegalArgumentException [itemList] が空の場合。
 */
internal data class DiaryDayList<T: DiaryDayListItem>(
    val itemList: List<T>
) {

    init {
        require(itemList.isNotEmpty()) { "空のDiaryDayListはインスタンス化できません。" }
    }

    /**
     * リストに含まれる日記アイテムの数を返す。
     *
     * @return リスト内のアイテム数。
     */
    fun countDiaries(): Int {
        return itemList.size
    }

    /**
     * このリストと指定された別の [DiaryDayList] を結合し、新しい [DiaryDayList] を返す。
     *
     * @param additionList このリストに結合する [DiaryDayList]。
     * @return ２つのリストを結合した新しい [DiaryDayList]。
     */
    fun combineDiaryDayLists(additionList: DiaryDayList<T>): DiaryDayList<T> {
        val resultItemList = itemList + additionList.itemList
        return DiaryDayList(resultItemList)
    }
}
