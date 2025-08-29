package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.ColumnInfo

/**
 * 日記リストのアイテムデータを表すデータクラス。
 *
 * このクラスは、日記一覧リストの各アイテムとして表示する情報を保持する。
 *
 * @property date 日記の日付。
 * @property title 日記のタイトル。
 * @property imageUriString 日記に添付した画像のURI文字列 (オプショナル)。
 */
internal data class DiaryListItemData(
    var date: String,
    var title: String,
    @ColumnInfo(name = "image_uri")
    var imageUriString: String?,
)
