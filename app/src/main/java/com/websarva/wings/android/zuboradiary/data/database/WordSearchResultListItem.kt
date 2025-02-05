package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.ColumnInfo

class WordSearchResultListItem : DiaryListBaseItem() {

    var title: String = ""

    @ColumnInfo(name = "item_1_title")
    var item1Title: String = ""

    @ColumnInfo(name = "item_1_comment")
    var item1Comment: String = ""

    @ColumnInfo(name = "item_2_title")
    var item2Title: String = ""

    @ColumnInfo(name = "item_2_comment")
    var item2Comment: String = ""

    @ColumnInfo(name = "item_3_title")
    var item3Title: String = ""

    @ColumnInfo(name = "item_3_comment")
    var item3Comment: String = ""

    @ColumnInfo(name = "item_4_title")
    var item4Title: String = ""

    @ColumnInfo(name = "item_4_comment")
    var item4Comment: String = ""

    @ColumnInfo(name = "item_5_title")
    var item5Title: String = ""

    @ColumnInfo(name = "item_5_comment")
    var item5Comment: String = ""
}
