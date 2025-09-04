package com.websarva.wings.android.zuboradiary.domain.usecase.exception

import com.websarva.wings.android.zuboradiary.domain.model.SavedDiaryDateRange


/**
 * 日記リストの先頭年月ピッカー用の日付範囲情報の取得処理中にエラーが発生した場合にスローされる例外。
 *
 * @param fallbackDateRange 日付範囲情報の取得に失敗した場合に使用される代替の日付範囲。
 *                          デフォルトでは、現在の日付を最新および最古とする範囲が設定される。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class LoadDiaryListStartYearMonthPickerDateRangeUseCaseException(
    val fallbackDateRange: SavedDiaryDateRange = SavedDiaryDateRange(),
    cause: Throwable
) : UseCaseException("日付範囲情報の取得に失敗しました。", cause)
