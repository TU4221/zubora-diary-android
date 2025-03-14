package com.websarva.wings.android.zuboradiary.ui.list.wordsearch

internal class WordSearchResultDayList {

    // TODO:下記変数をprivateでカプセル化し、必要なメソッドは本クラスで新規に作成する。(countDiaries()のように)(他のクラスも同様にする)
    val wordSearchResultDayListItemList: List<WordSearchResultDayListItem>

    constructor(itemList: List<WordSearchResultDayListItem>) {
        this.wordSearchResultDayListItemList = itemList.toList()
    }

    constructor() : this(ArrayList())

    fun countDiaries(): Int {
        return wordSearchResultDayListItemList.size
    }

    fun combineDiaryDayLists(additionList: WordSearchResultDayList): WordSearchResultDayList {
        require(additionList.wordSearchResultDayListItemList.isNotEmpty())

        val resultItemList =
            wordSearchResultDayListItemList + additionList.wordSearchResultDayListItemList
        return WordSearchResultDayList(resultItemList)
    }
}
