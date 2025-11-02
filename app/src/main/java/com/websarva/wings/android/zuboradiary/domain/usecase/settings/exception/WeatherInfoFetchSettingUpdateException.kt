package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
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
     * @param setting 更新しようとした設定 [WeatherInfoFetchSetting] 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        setting: WeatherInfoFetchSetting,
        cause: Throwable
    ) : WeatherInfoFetchSettingUpdateException(
        createExceptionBaseMessage(setting),
        cause
    )

    /**
     * ストレージ容量不足により、天気情報取得設定の更新に失敗した場合にスローされる例外。
     *
     * @param setting 更新しようとした設定 [WeatherInfoFetchSetting] 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class InsufficientStorage(
        setting: WeatherInfoFetchSetting,
        cause: Throwable
    ) : WeatherInfoFetchSettingUpdateException(
        "ストレージ容量不足により、" + createExceptionBaseMessage(setting),
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : WeatherInfoFetchSettingUpdateException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException

    companion object {
        /**
         * 指定された天気情報取得設定に基づき、更新失敗エラーメッセージの共通部分を生成する。
         *
         * 具体的なエラー原因は含めず、どの設定の更新に失敗したかを識別するための基本メッセージとなる。
         *
         * @param setting 更新に失敗した [WeatherInfoFetchSetting]。
         * @return 更新対象の設定値を含む、基本的なエラーメッセージ文字列。
         *         例: "天気情報取得設定 '有効' の更新に失敗しました。"
         */
        private fun createExceptionBaseMessage(setting: WeatherInfoFetchSetting): String {
            val status = if (setting.isEnabled) "有効" else "無効"
            return "天気情報取得設定 '$status' の更新に失敗しました。"
        }
    }
}
