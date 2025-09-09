package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.InitializeAllSettingsUseCase

/**
 * [InitializeAllSettingsUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class AllSettingsInitializationException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * テーマカラー設定の初期化に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class ThemeColorInitializationFailure(
        cause: Throwable
    ) : AllSettingsInitializationException(
        "テーマカラー設定の初期化に失敗しました。",
        cause
    )

    /**
     * カレンダー開始曜日設定の初期化に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class CalendarStartDayOfWeeksInitializationFailure(
        cause: Throwable
    ) : AllSettingsInitializationException(
        "カレンダー開始曜日設定の初期化に失敗しました。",
        cause
    )

    /**
     * リマインダー通知設定の初期化に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class ReminderNotificationInitializationFailure(
        cause: Throwable
    ) : AllSettingsInitializationException(
        "リマインダー通知設定の初期化に失敗しました。",
        cause
    )

    /**
     * パスコード設定の初期化に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class PasscodeInitializationFailure(
        cause: Throwable
    ) : AllSettingsInitializationException(
        "パスコード設定の初期化に失敗しました。",
        cause
    )

    /**
     * 天気情報取得設定の初期化に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class WeatherInfoFetchInitializationFailure(
        cause: Throwable
    ) : AllSettingsInitializationException(
        "天気情報取得設定の初期化に失敗しました。",
        cause
    )
}
