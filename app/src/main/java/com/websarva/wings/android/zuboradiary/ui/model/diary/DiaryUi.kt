package com.websarva.wings.android.zuboradiary.ui.model.diary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime

// TODO:未使用だが開発最終に削除する
/**
 * 日記を表すデータクラス。
 *
 * このクラスは、特定の日付の日記の内容（天気、体調、タイトル、項目、画像ファイル名など）を保持する。
 *
 * アイテムは最大5つまで設定でき、itemXTitle と itemXComment はペアでnullまたは非nullである必要がある。
 * また、item(N)が設定されている場合、item(N-1)も設定されている必要がある。
 *
 * @property id 日記の識別番号。
 * @property date 日記の日付。
 * @property log 最終更新日時。
 * @property weather1 その日の天気（1つ目）。
 * @property weather2 その日の天気（2つ目）。
 * @property condition その日の体調。
 * @property title 日記のタイトル。
 * @property item1Title 1番目の日記項目のタイトル。
 * @property item1Comment 1番目の日記項目のコメント。
 * @property item2Title 2つ目の項目のタイトル。
 * @property item2Comment 2つ目の項目のコメント。
 * @property item3Title 3つ目の項目のタイトル。
 * @property item3Comment 3つ目の項目のコメント。
 * @property item4Title 4つ目の項目のタイトル。
 * @property item4Comment 4つ目の項目のコメント。
 * @property item5Title 5つ目の項目のタイトル。
 * @property item5Comment 5つ目の項目のコメント。
 * @property imageFileName 日記に添付した画像ファイル名。未添付の場合 `null`。
 */
@Parcelize
internal data class DiaryUi(
    val id: String,
    val date: LocalDate,
    val log: LocalDateTime,
    val weather1: WeatherUi,
    val weather2: WeatherUi,
    val condition: ConditionUi,
    val title: String,
    val item1Title: String,
    val item1Comment: String,
    val item2Title: String?,
    val item2Comment: String?,
    val item3Title: String?,
    val item3Comment: String?,
    val item4Title: String?,
    val item4Comment: String?,
    val item5Title: String?,
    val item5Comment: String?,
    val imageFileName: String?
) : Parcelable
