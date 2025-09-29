package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.SavedDiaryDateRange
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryListStartYearMonthPickerDateRangeUseCase


/**
 * [LoadDiaryListStartYearMonthPickerDateRangeUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal sealed class DiaryListStartYearMonthPickerDateRangeLoadException(
    val fallbackDateRange: SavedDiaryDateRange = SavedDiaryDateRange(),
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
        cause: Throwable
    ) : DiaryListStartYearMonthPickerDateRangeLoadException(message = "日付範囲情報の取得に失敗しました。", cause = cause)

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryListStartYearMonthPickerDateRangeLoadException(
        message = "予期せぬエラーが発生しました。",
        cause = cause
    )
}
