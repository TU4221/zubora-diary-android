package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// MEMO:@NonNullに関する警告はROOMの機能で解消されるため無視すること。
@Entity(tableName = "diaries")
class DiaryEntity {

    // TODO:全プロパティを非null型に変更する。
    @PrimaryKey
    var date: String = ""

    var log: String = ""

    @ColumnInfo(name = "weather_1") //@NonNull
    var weather1: Int? = null

    @ColumnInfo(name = "weather_2") //@NonNull
    var weather2: Int? = null

    //@NonNull
    var condition: Int? = null

    //@NonNull
    var title: String? = null

    @ColumnInfo(name = "item_1_title") //@NonNull
    var item1Title: String? = null

    @ColumnInfo(name = "item_1_comment") //@NonNull
    var item1Comment: String? = null

    @ColumnInfo(name = "item_2_title") //@NonNull
    var item2Title: String? = null

    @ColumnInfo(name = "item_2_comment") //@NonNull
    var item2Comment: String? = null

    @ColumnInfo(name = "item_3_title") //@NonNull
    var item3Title: String? = null

    @ColumnInfo(name = "item_3_comment") //@NonNull
    var item3Comment: String? = null

    @ColumnInfo(name = "item_4_title") //@NonNull
    var item4Title: String? = null

    @ColumnInfo(name = "item_4_comment") //@NonNull
    var item4Comment: String? = null

    @ColumnInfo(name = "item_5_title") //@NonNull
    var item5Title: String? = null

    @ColumnInfo(name = "item_5_comment") //@NonNull
    var item5Comment: String? = null

    //@NonNull
    var picturePath: String? = null
}
