package com.websarva.wings.android.zuboradiary.domain.usecase.exception

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.SavedDiaryDateRange
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryListStartYearMonthPickerDateRangeUseCase


/**
 * [LoadDiaryListStartYearMonthPickerDateRangeUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal sealed class DiaryListStartYearMonthPickerDateRangeLoadException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記リストの先頭年月ピッカー用の日付範囲情報の読み込みに失敗した場合にスローされる例外。
     *
     * @param fallbackDateRange 日付範囲情報の取得に失敗した場合に使用される代替の日付範囲。
     *                          デフォルトでは、現在の日付を最新および最古とする範囲が設定される。
     * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
     */
    class DiaryInfoLoadFailure(
        val fallbackDateRange: SavedDiaryDateRange = SavedDiaryDateRange(),
        cause: Throwable
    ) : DiaryListStartYearMonthPickerDateRangeLoadException("日付範囲情報の取得に失敗しました。", cause)
}
