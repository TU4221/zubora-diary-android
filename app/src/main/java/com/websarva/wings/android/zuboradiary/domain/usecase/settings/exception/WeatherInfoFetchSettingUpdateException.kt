package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateWeatherInfoFetchSettingUseCase

/**
 * [UpdateWeatherInfoFetchSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class WeatherInfoFetchSettingUpdateException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 天気情報取得設定の更新に失敗した場合にスローされる例外。
     *
     * @param setting 更新しようとした設定 [WeatherInfoFetchSetting] オブジェクト。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        setting: WeatherInfoFetchSetting,
        cause: Throwable
    ) : WeatherInfoFetchSettingUpdateException(
        "天気情報取得設定 '${
            if (setting.isEnabled) {
                "有効"
            } else {
                "無効"
            }
        }' の更新に失敗しました。"
        , cause
    )
}
