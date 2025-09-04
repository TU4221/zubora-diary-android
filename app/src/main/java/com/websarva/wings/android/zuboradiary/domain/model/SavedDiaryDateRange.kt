package com.websarva.wings.android.zuboradiary.domain.model

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
internal data class SavedDiaryDateRange(
    val newestDiaryDate: LocalDate = LocalDate.now(),
    val oldestDiaryDate: LocalDate = LocalDate.now()
) {
    init {
        require(newestDiaryDate >= oldestDiaryDate) {
            "不正引数_最新の日記の日付が最古の日記の日付よりも古い (最新日記日付: ${newestDiaryDate}, 最古日記日付: ${oldestDiaryDate})"
        }
    }
}
