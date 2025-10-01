package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime


/**
 * 日記の単一のエントリを表すRoomエンティティクラス。
 *
 * このクラスは、データベースの "diaries" テーブルの各行に対応する。
 * 日記内容を保持する。
 *
 * @property id 日記の識別番号。このエンティティの主キー。
 * @property date 日記の日付。
 * @property log 日記の更新日付。
 * @property weather1 1つ目の天気情報を示す整数値。具体的な値はアプリの定義による。
 * @property weather2 2つ目の天気情報を示す整数値。オプションであり、1つ目と異なる場合や補足的な情報として使用。
 * @property condition その日の体調や気分を示す整数値。具体的な値はアプリの定義による。
 * @property title 日記のタイトル。
 * @property item1Title 1つ目の項目のタイトル。
 * @property item1Comment 1つ目の項目のコメント。
 * @property item2Title 2つ目の項目のタイトル。未入力の場合 `null`。
 * @property item2Comment 2つ目の項目のコメント。未入力の場合 `null`。
 * @property item3Title 3つ目の項目のタイトル。未入力の場合 `null`。
 * @property item3Comment 3つ目の項目のコメント。未入力の場合 `null`。
 * @property item4Title 4つ目の項目のタイトル。未入力の場合 `null`。
 * @property item4Comment 4つ目の項目のコメント。未入力の場合 `null`。
 * @property item5Title 5つ目の項目のタイトル。未入力の場合 `null`。
 * @property item5Comment 5つ目の項目のコメント。未入力の場合 `null`。
 * @property imageFileName 日記に添付した画像ファイル名。未添付の場合 `null`。
 */
@Entity(tableName = "diaries")
internal data class DiaryEntity(
    @PrimaryKey
    val id: String,

    val date: LocalDate,

    val log: LocalDateTime,

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
    val item2Title: String?,

    @ColumnInfo(name = "item_2_comment")
    val item2Comment: String?,

    @ColumnInfo(name = "item_3_title")
    val item3Title: String?,

    @ColumnInfo(name = "item_3_comment")
    val item3Comment: String?,

    @ColumnInfo(name = "item_4_title")
    val item4Title: String?,

    @ColumnInfo(name = "item_4_comment")
    val item4Comment: String?,

    @ColumnInfo(name = "item_5_title")
    val item5Title: String?,

    @ColumnInfo(name = "item_5_comment")
    val item5Comment: String?,

    @ColumnInfo(name = "image_file_name")
    val imageFileName: String?
)
