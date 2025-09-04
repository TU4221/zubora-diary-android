package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * カレンダーの週の開始曜日設定を読み込むユースケース。
 *
 * 設定の読み込み結果を [Flow] として提供する。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class LoadCalendarStartDayOfWeekSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "カレンダー開始曜日設定読込_"

    /**
     * ユースケースを実行し、カレンダーの週の開始曜日設定の読み込み結果を [Flow] として返す。
     *
     * 読み込み処理はリポジトリ層で行われ、その結果を [UserSettingResult] に変換して [UseCaseResult] にラップする。
     * - データソースからの読み込み成功時: [UserSettingResult.Success] を発行。
     * - データソースからの読み込み失敗時 (アクセス失敗): [UserSettingResult.Failure] を発行し、
     *   デフォルト値をフォールバックとして提供する。
     * - データソースからの読み込み失敗時 (データ未発見): [UserSettingResult.Success] を発行し、
     *   デフォルト値を設定値として提供する。
     *
     * @return カレンダーの週の開始曜日設定の読み込み結果 ([UserSettingResult]) を
     *   [Flow] でラップし、[UseCaseResult.Success] に格納して返す。
     *   このユースケースは、常に [UseCaseResult.Success] を返す。
     */
    operator fun invoke(): UseCaseResult.Success<Flow<UserSettingResult<CalendarStartDayOfWeekSetting>>> {
        Log.i(logTag, "${logMsg}開始")

        val value =
            settingsRepository
                .loadCalendarStartDayOfWeekPreference()
                .map { result: UserSettingDataSourceResult<CalendarStartDayOfWeekSetting> ->
                    when (result) {
                        is UserSettingDataSourceResult.Success -> {
                            Log.d(
                                logTag,
                                "${logMsg}読込成功 (設定値: ${result.setting})"
                            )
                            UserSettingResult.Success(result.setting)
                        }
                        is UserSettingDataSourceResult.Failure -> {
                            val defaultSettingValue = CalendarStartDayOfWeekSetting()
                            when (result.exception) {
                                is UserSettingsLoadException.AccessFailure -> {
                                    Log.w(
                                        logTag,
                                        "${logMsg}失敗_アクセス失敗、" +
                                                "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                        result.exception
                                    )
                                    UserSettingResult.Failure(
                                        result.exception,
                                        defaultSettingValue
                                    )
                                }
                                is UserSettingsLoadException.DataNotFound -> {
                                    Log.i(
                                        logTag,
                                        "${logMsg}失敗_データ未発見、" +
                                                "デフォルト値を設定値として使用 (デフォルト値: $defaultSettingValue)"
                                    )
                                    UserSettingResult.Success(
                                        defaultSettingValue
                                    )
                                }
                            }
                        }
                    }
                }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(value)
    }
}
