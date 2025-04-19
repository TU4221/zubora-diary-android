package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diaries")
internal data class DiaryEntity(
    @PrimaryKey
    val date: String,

    val log: String,

    @ColumnInfo(name = "weather_1")
    val weather1: Int,

    @ColumnInfo(name = "weather_2")
    val weather2: Int,

    val condition: Int,

    val title: String,

    @ColumnInfo(name = "item_1_title")
    val item1Title: String,

    @ColumnInfo(name = "item_1_comment")
    val item1Comment: String,

    @ColumnInfo(name = "item_2_title")
    val item2Title: String,

    @ColumnInfo(name = "item_2_comment")
    val item2Comment: String,

    @ColumnInfo(name = "item_3_title")
    val item3Title: String,

    @ColumnInfo(name = "item_3_comment")
    val item3Comment: String,

    @ColumnInfo(name = "item_4_title")
    val item4Title: String,

    @ColumnInfo(name = "item_4_comment")
    val item4Comment: String,

    @ColumnInfo(name = "item_5_title")
    val item5Title: String,

    @ColumnInfo(name = "item_5_comment")
    val item5Comment: String,

    val picturePath: String
)
