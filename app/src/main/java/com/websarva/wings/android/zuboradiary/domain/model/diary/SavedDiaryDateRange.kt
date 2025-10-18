package com.websarva.wings.android.zuboradiary.domain.model.diary

import com.websarva.wings.android.zuboradiary.domain.model.serializer.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
import java.time.LocalDate

/**
 * 保存されている日記データの期間範囲を表すデータクラス。
 *
 * このクラスは、記録されている日記の中で最も新しい日付と最も古い日付を保持する。
 *
 * @property newestDiaryDate 保存されている日記の中で最も新しい日付。デフォルトは現在の日付。
 * @property oldestDiaryDate 保存されている日記の中で最も古い日付。デフォルトは現在の日付。
 * @throws IllegalArgumentException [newestDiaryDate] が [oldestDiaryDate] よりも古い場合にスローされる。
 */
@Serializable
internal data class SavedDiaryDateRange(
    @Serializable(with = LocalDateSerializer::class)
    val newestDiaryDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val oldestDiaryDate: LocalDate
) : JavaSerializable {
    init {
        require(newestDiaryDate >= oldestDiaryDate) {
            "不正引数_最新の日記の日付が最古の日記の日付よりも古い (最新日記日付: ${newestDiaryDate}, 最古日記日付: ${oldestDiaryDate})"
        }
    }

    companion object {
        /**
         * 日記が一件も存在しない場合のデフォルトの状態を表すインスタンスを返します。
         * 最新日と最古日が同じ今日の日付になります。
         */
        fun empty(): SavedDiaryDateRange {
            val today = LocalDate.now()
            return SavedDiaryDateRange(today, today)
        }
    }
}
