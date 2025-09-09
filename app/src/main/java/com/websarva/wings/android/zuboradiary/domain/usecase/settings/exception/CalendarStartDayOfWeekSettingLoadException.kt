package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadCalendarStartDayOfWeekSettingUseCase

/**
 * [LoadCalendarStartDayOfWeekSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 * カレンダーの開始曜日設定の読み込み処理中にエラーが発生した場合にスローされる例外基底クラス。。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class CalendarStartDayOfWeekSettingLoadException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * カレンダーの開始曜日設定の読み込みに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        cause: Throwable
    ) : CalendarStartDayOfWeekSettingLoadException("カレンダー開始曜日設定の読込に失敗しました。", cause)
}
