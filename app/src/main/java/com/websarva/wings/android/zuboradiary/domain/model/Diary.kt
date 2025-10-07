package com.websarva.wings.android.zuboradiary.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 日記を表すデータクラス。
 *
 * このクラスは、特定の日付の日記の内容（天気、体調、タイトル、項目、画像ファイル名など）を保持する。
 *
 * アイテムは最大5つまで設定でき、itemXTitle と itemXComment はペアでnullまたは非nullである必要がある。
 * また、item(N)が設定されている場合、item(N-1)も設定されている必要がある。
 *
 * @property id 日記の識別番号。デフォルトはUUIDランダム生成値。
 * @property date 日記の日付。デフォルトは現在の日付。
 * @property log 最終更新日時。デフォルトは現在のローカル日時。
 * @property weather1 その日の天気（1つ目）。デフォルトは [Weather.UNKNOWN]。
 * @property weather2 その日の天気（2つ目、任意）。デフォルトは [Weather.UNKNOWN]。
 * @property condition その日の体調。デフォルトは [Condition.UNKNOWN]。
 * @property title 日記のタイトル。デフォルトは空文字列。
 * @property item1Title 1番目の日記項目のタイトル。デフォルトは空文字列。
 * @property item1Comment 1番目の日記項目のコメント。デフォルトは空文字列。
 * @property item2Title 2つ目の項目のタイトル。未入力の場合 `null`。
 * @property item2Comment 2つ目の項目のコメント。未入力の場合 `null`。
 * @property item3Title 3つ目の項目のタイトル。未入力の場合 `null`。
 * @property item3Comment 3つ目の項目のコメント。未入力の場合 `null`。
 * @property item4Title 4つ目の項目のタイトル。未入力の場合 `null`。
 * @property item4Comment 4つ目の項目のコメント。未入力の場合 `null`。
 * @property item5Title 5つ目の項目のタイトル。未入力の場合 `null`。
 * @property item5Comment 5つ目の項目のコメント。未入力の場合 `null`。
 * @property imageFileName 日記に添付した画像ファイル名。未添付の場合 `null`。
 * @throws IllegalArgumentException 日記項目のタイトルとコメントのnull整合性、または日記項目の順序整合性に違反する場合。
 */
internal data class Diary(
    val id: DiaryId = DiaryId(),
    val date: LocalDate = LocalDate.now(),
    val log: LocalDateTime = LocalDateTime.now(),
    val weather1: Weather = Weather.UNKNOWN,
    val weather2: Weather = Weather.UNKNOWN,
    val condition: Condition = Condition.UNKNOWN,
    val title: String = "",
    val item1Title: String = "",
    val item1Comment: String = "",
    val item2Title: String? = null,
    val item2Comment: String? = null,
    val item3Title: String? = null,
    val item3Comment: String? = null,
    val item4Title: String? = null,
    val item4Comment: String? = null,
    val item5Title: String? = null,
    val item5Comment: String? = null,
    val imageFileName: FileName? = null
) {

    init {
        val items = listOf(
            item1Title to item1Comment,
            item2Title to item2Comment,
            item3Title to item3Comment,
            item4Title to item4Comment,
            item5Title to item5Comment
        )
        for (i in 1 until items.size) {
            val currentItemNumber = i + 1
            val currentTitle = items[i].first
            val currentComment = items[i].second
            val previousItemNumber = currentItemNumber - 1
            val previousTitle = items[i - 1].first

            if (currentTitle == null && currentComment == null) continue
            if (currentTitle == null) {
                throw IllegalArgumentException(
                    "item${currentItemNumber}Titleがnullの為、item${currentItemNumber}Commentもnullであるべきです。"
                )
            }
            if (currentComment == null) {
                throw IllegalArgumentException(
                    "item${currentItemNumber}Commentがnullの為、item${currentItemNumber}Titleもnullであるべきです。"
                )
            }
            if (previousTitle == null) {
                throw IllegalArgumentException(
                    "item${previousItemNumber}がnullの為、item${currentItemNumber}もnullであるべきです。"
                )
            }
        }
    }

    /**
     * Diaryオブジェクトの内容を比較する。
     * `log` プロパティは比較対象から除外される。
     *
     * @param other 比較対象のDiaryオブジェクト。
     * @return `log` を除いた他の全てのプロパティが等しい場合はtrue、そうでない場合はfalse。
     */
    fun isContentEqualToIgnoringLog(other: Diary?): Boolean {
        // 同じインスタンスまたは相手がnullの場合は早期リターン
        if (this === other) return true
        if (other == null) return false

        // log以外の全てのプロパティを比較
        return this.date == other.date &&
                // this.log == other.log (この行を除外)
                this.weather1 == other.weather1 &&
                this.weather2 == other.weather2 &&
                this.condition == other.condition &&
                this.title == other.title &&
                this.item1Title == other.item1Title &&
                this.item1Comment == other.item1Comment &&
                this.item2Title == other.item2Title &&
                this.item2Comment == other.item2Comment &&
                this.item3Title == other.item3Title &&
                this.item3Comment == other.item3Comment &&
                this.item4Title == other.item4Title &&
                this.item4Comment == other.item4Comment &&
                this.item5Title == other.item5Title &&
                this.item5Comment == other.item5Comment &&
                this.imageFileName == other.imageFileName
    }
}
