package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveCalendarStartDayOfWeekUseCase
import java.time.DayOfWeek

/**
 * [SaveCalendarStartDayOfWeekUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
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
     * @param dayOfWeek 更新しようとしたカレンダーの開始曜日。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        dayOfWeek: DayOfWeek,
        cause: Throwable
    ) : CalendarStartDayOfWeekSettingUpdateException(
        "カレンダー開始曜日設定 '$dayOfWeek' の更新に失敗しました。",
        cause
    )
}
