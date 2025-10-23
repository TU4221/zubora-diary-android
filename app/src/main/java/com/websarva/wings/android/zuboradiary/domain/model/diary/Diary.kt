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
 * インスタンスの生成は [Diary.create] ファクトリメソッドを通じて行われ、
 * 日記項目の整合性（例：項目間の空でないこと、ペアでのnull状態）が保証される。
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
 */
@ConsistentCopyVisibility
@Serializable
internal data class Diary private constructor(
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
         * Diaryのインスタンスを生成するためのファクトリメソッド。
         *
         * このメソッドは、日記項目の整合性を保証するためのロジックを含む。
         */
        fun create(
            id: DiaryId,
            date: LocalDate,
            log: LocalDateTime,
            weather1: Weather,
            weather2: Weather,
            condition: Condition,
            title: DiaryTitle,
            itemTitles: Map<Int, DiaryItemTitle?>,
            itemComments: Map<Int, DiaryItemComment?>,
            imageFileName: DiaryImageFileName?
        ): Diary {
            val normalizedTitles = itemTitles.toMutableMap()
            val normalizedComments = itemComments.toMutableMap()

            // 1. タイトルとコメントのペアを同期(片方がnon-nullならもう片方も空でnon-nullにする)
            for (i in 1..5) {
                val currentTitle = normalizedTitles[i]
                val currentComment = normalizedComments[i]
                if (currentTitle != null && currentComment == null) {
                    normalizedComments[i] = DiaryItemComment.empty()
                }
                if (currentTitle == null && currentComment != null) {
                    normalizedTitles[i] = DiaryItemTitle.empty()
                }
            }

            // 2. 項目1は常にnon-nullであることを保証する
            if (normalizedTitles[1] == null) { // この時点でコメントもnullのはず
                normalizedTitles[1] = DiaryItemTitle.empty()
                normalizedComments[1] = DiaryItemComment.empty()
            }

            // 3. 項目2～5で、タイトルとコメントが両方空文字列ならペアをnullに変換
            for (i in 2..5) {
                val currentTitle = normalizedTitles[i]
                val currentComment = normalizedComments[i]
                if (currentTitle != null
                    && currentComment != null
                    && currentTitle.value.isEmpty()
                    && currentComment.value.isEmpty()) {
                    normalizedTitles[i] = null
                    normalizedComments[i] = null
                }
            }

            // 4. 最後のnon-null項目までの全ての項目がnon-nullであることを保証
            val lastNonNull = (5 downTo 1).firstOrNull { normalizedTitles[it] != null }
            if (lastNonNull != null) {
                for (i in 1 until lastNonNull) {
                    if (normalizedTitles[i] == null) { // この時点でコメントもnull
                        normalizedTitles[i] = DiaryItemTitle.empty()
                        normalizedComments[i] = DiaryItemComment.empty()
                    }
                }
            }

            return Diary(
                id,
                date,
                log,
                weather1,
                weather2,
                condition,
                title,
                normalizedTitles.toMap(),
                normalizedComments.toMap(),
                imageFileName
            )
        }

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
