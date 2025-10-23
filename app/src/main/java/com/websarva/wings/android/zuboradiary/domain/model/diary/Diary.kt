package com.websarva.wings.android.zuboradiary.domain.model.diary

import com.websarva.wings.android.zuboradiary.core.serializer.LocalDateSerializer
import com.websarva.wings.android.zuboradiary.core.serializer.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
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
 * @property itemTitles 日記項目のタイトルのマップ。キーは項目の連番(1-5)。
 * @property itemComments 日記項目のコメントのマップ。キーは項目の連番(1-5)。
 * @property imageFileName 日記に添付した画像ファイル名。未添付の場合 `null`。
 * @throws IllegalArgumentException 日記項目のタイトルとコメントのnull整合性、または日記項目の順序整合性に違反する場合。
 */
@Serializable
internal data class Diary(
    val id: DiaryId,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    @Serializable(with = LocalDateTimeSerializer::class)
    val log: LocalDateTime,
    val weather1: Weather,
    val weather2: Weather,
    val condition: Condition,
    val title: DiaryTitle,
    val itemTitles: Map<Int, DiaryItemTitle?>,
    val itemComments: Map<Int, DiaryItemComment?>,
    val imageFileName: DiaryImageFileName?
) : JavaSerializable {

    init {
        for (i in 1..5) {
            val title = itemTitles[i]
            val comment = itemComments[i]
            if ((title == null) != (comment == null)) {
                throw IllegalArgumentException(
                    "item${i}Title and item${i}Comment は両方null、又は両方非nullでであるべき。"
                )
            }

            if (i > 1) {
                val hasCurrent = title != null
                val hasPrevious = itemTitles[i-1] != null
                if (hasCurrent && !hasPrevious) {
                    throw IllegalArgumentException(
                        "item${i - 1}がnullの為、item${i}もnullであるべき。"
                    )
                }
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
                this.itemTitles == other.itemTitles &&
                this.itemComments == other.itemComments &&
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
         * - `itemTitles`, `itemComments`: 1番目の項目のみ空の状態で存在する
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
                mapOf(1 to DiaryItemTitle.empty()),
                mapOf(1 to DiaryItemComment.empty()),
                null
            )
    }
}
