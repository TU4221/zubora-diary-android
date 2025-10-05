package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.ColumnInfo
import java.time.LocalDate

/**
 * 日記リストのアイテムデータを表すデータクラス。
 *
 * このクラスは、日記一覧リストの各アイテムとして表示する情報を保持する。
 *
 * @property id 日記のID。
 * @property date 日記の日付。
 * @property title 日記のタイトル。
 * @property imageFileName 日記に添付した画像ファイル名。未添付の場合 `null`。
 */
internal data class DiaryListItemData(
    val id: String,
    val date: LocalDate,
    val title: String,
    @ColumnInfo(name = "image_file_name")
    val imageFileName: String?
)
