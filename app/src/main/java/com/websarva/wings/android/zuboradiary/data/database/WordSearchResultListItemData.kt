package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.ColumnInfo

/**
 * 単語検索結果のリストアイテムデータを表すデータクラス。
 *
 * このクラスは、検索結果一覧リストの各アイテムとして表示する情報を保持する。
 *
 * @property date 日記の日付。
 * @property title 日記のタイトル。
 * @property item1Title 1つ目の項目のタイトル。
 * @property item1Comment 1つ目の項目のコメント。
 * @property item2Title 2つ目の項目のタイトル (オプショナル)。
 * @property item2Comment 2つ目の項目のコメント (オプショナル)。
 * @property item3Title 3つ目の項目のタイトル (オプショナル)。
 * @property item3Comment 3つ目の項目のコメント (オプショナル)。
 * @property item4Title 4つ目の項目のタイトル (オプショナル)。
 * @property item4Comment 4つ目の項目のコメント (オプショナル)。
 * @property item5Title 5つ目の項目のタイトル (オプショナル)。
 * @property item5Comment 5つ目の項目のコメント (オプショナル)。
 */
internal data class WordSearchResultListItemData(
    val date: String,
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
    val item5Comment: String?
)
