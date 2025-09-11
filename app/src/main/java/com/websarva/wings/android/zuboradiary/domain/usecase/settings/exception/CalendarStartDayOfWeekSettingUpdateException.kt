package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateCalendarStartDayOfWeekSettingUseCase

/**
 * [UpdateCalendarStartDayOfWeekSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class CalendarStartDayOfWeekSettingUpdateException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * カレンダーの開始曜日設定の更新に失敗した場合にスローされる例外。
     *
     * @param setting 更新しようとした設定 [CalendarStartDayOfWeekSetting] 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        setting: CalendarStartDayOfWeekSetting,
        cause: Throwable
    ) : CalendarStartDayOfWeekSettingUpdateException(
        "カレンダー開始曜日設定 '${setting.dayOfWeek}' の更新に失敗しました。",
        cause
    )
}
