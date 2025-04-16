package com.websarva.wings.android.zuboradiary.ui.adapter.diary.wordsearchresult

internal class WordSearchResultDayList {

    // TODO:下記変数をprivateでカプセル化し、必要なメソッドは本クラスで新規に作成する。(countDiaries()のように)(他のクラスも同様にする)
    val itemList: List<WordSearchResultDayListItem>

    constructor(itemList: List<WordSearchResultDayListItem>) {
        this.itemList = itemList.toList()
    }

    constructor() : this(ArrayList())

    fun countDiaries(): Int {
        return itemList.size
    }

    fun combineDiaryDayLists(additionList: WordSearchResultDayList): WordSearchResultDayList {
        require(additionList.itemList.isNotEmpty())

        val resultItemList =
            itemList + additionList.itemList
        return WordSearchResultDayList(resultItemList)
    }
}
