package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NotFoundException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
     * @return カレンダーの週の開始曜日設定の読み込み結果を [UseCaseResult] へ [Flow] でラップして返す。
     *   読み込みが成功した場合は[UseCaseResult.Success] に [CalendarStartDayOfWeekSetting] を格納して返す。
     *   読み込みに失敗した場合は、[UseCaseResult.Failure] にフォールバック値を格納した
     *   [CalendarStartDayOfWeekSettingLoadException] を格納して返す。
     */
    operator fun invoke(): Flow<
            UseCaseResult<CalendarStartDayOfWeekSetting, CalendarStartDayOfWeekSettingLoadException>
    > {
        Log.i(logTag, "${logMsg}開始")

        return settingsRepository
            .loadCalendarStartDayOfWeekSetting()
            .map { setting: CalendarStartDayOfWeekSetting ->
                Log.d(
                    logTag,
                    "${logMsg}読込成功 (設定値: ${setting})"
                )
                val result: UseCaseResult<
                        CalendarStartDayOfWeekSetting,
                        CalendarStartDayOfWeekSettingLoadException
                > = UseCaseResult.Success(setting)
                result
            }.catch { cause: Throwable ->
                val defaultSettingValue = CalendarStartDayOfWeekSetting()
                val result =
                    when (cause) {
                        is DataStorageException -> {
                            Log.w(
                                logTag,
                                "${logMsg}失敗_アクセス失敗、" +
                                        "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                cause
                            )
                            UseCaseResult.Failure(
                                CalendarStartDayOfWeekSettingLoadException
                                    .LoadFailure(defaultSettingValue, cause),
                            )
                        }
                        is NotFoundException -> {
                            Log.i(
                                logTag,
                                "${logMsg}_データ未発見_" +
                                        "デフォルト値を設定値として使用 (デフォルト値: $defaultSettingValue)"
                            )
                            UseCaseResult.Success(defaultSettingValue)
                        }
                        else -> throw cause
                    }
                emit(result)
            }
    }
}
