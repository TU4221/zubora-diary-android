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
 * @property id 日記の識別番号。
 * @property date 日記の日付。
 * @property log 最終更新日時。
 * @property weather1 その日の天気（1つ目）。
 * @property weather2 その日の天気（2つ目、任意）。
 * @property condition その日の体調。
 * @property title 日記のタイトル。
 * @property item1Title 1番目の日記項目のタイトル。
 * @property item1Comment 1番目の日記項目のコメント。
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
    val id: DiaryId,
    val date: LocalDate,
    val log: LocalDateTime,
    val weather1: Weather,
    val weather2: Weather,
    val condition: Condition,
    val title: DiaryTitle,
    val item1Title: DiaryItemTitle,
    val item1Comment: DiaryItemComment,
    val item2Title: DiaryItemTitle?,
    val item2Comment: DiaryItemComment?,
    val item3Title: DiaryItemTitle?,
    val item3Comment: DiaryItemComment?,
    val item4Title: DiaryItemTitle?,
    val item4Comment: DiaryItemComment?,
    val item5Title: DiaryItemTitle?,
    val item5Comment: DiaryItemComment?,
    val imageFileName: DiaryImageFileName?
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

    companion object {
        /**
         * 新しい [Diary] を生成する。
         *
         * 生成されるDiaryは以下の初期値を持つ:
         * - `id`: ランダムな [DiaryId]
         * - `date`: 現在の日付 ([LocalDate.now])
         * - `log`: 現在の日時 ([LocalDateTime.now])
         * - `weather1`, `weather2`: [Weather.UNKNOWN]
         * - `condition`: [Condition.UNKNOWN]
         * - `title`: 空のタイトル
         * - `item1Title`, `item1Comment`: 空の項目
         * - `item2` から `item5`: `null`
         * - `imageFileName`: `null`
         *
         */
        fun generate() =
            Diary(
                DiaryId.generate(),
                LocalDate.now(),
                LocalDateTime.now(),
                Weather.UNKNOWN,
                Weather.UNKNOWN,
                Condition.UNKNOWN,
                DiaryTitle(""),
                DiaryItemTitle.empty(),
                DiaryItemComment.empty(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
    }
}
