package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * カレンダーの週の開始曜日設定を読み込むユースケース。
 *
 * 設定の読み込み結果を [Flow] として提供する。
 *
 * このユースケースは、アプリの動作に不可欠な設定値を取得するため、
 * データソース層からいかなる種類の例外（RuntimeExceptionなどを含む）がスローされた場合でも、
 * ViewModelに例外を再スローせず、必ずフォールバック値を含む [UseCaseResult] を返す。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class LoadCalendarStartDayOfWeekSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logMsg = "カレンダー開始曜日設定読込_"

    /**
     * ユースケースを実行し、カレンダーの週の開始曜日設定の読み込み結果を [Flow] として返す。
     *
     * @return 読み込み結果を [UseCaseResult] へ [Flow] 内部でラップして返す。
     *   読み込みに成功した場合は[UseCaseResult.Success] に
     *   カレンダーの週の開始曜日設定( [CalendarStartDayOfWeekSetting] )を格納して返す。
     *   失敗した場合は、[UseCaseResult.Failure] にフォールバック値を格納した
     *   [CalendarStartDayOfWeekSettingLoadException] を格納して返す。
     */
    operator fun invoke(): Flow<
            UseCaseResult<CalendarStartDayOfWeekSetting, CalendarStartDayOfWeekSettingLoadException>
    > {
        Log.i(logTag, "${logMsg}開始")

        val defaultSettingValue = CalendarStartDayOfWeekSetting.default()
        return settingsRepository
            .loadCalendarStartDayOfWeekSetting()
            .map { setting ->
                Log.d(
                    logTag,
                    "${logMsg}読込成功 (${setting?.let { "設定値: $it" } ?: "未設定"})"
                )
                val result: UseCaseResult<
                        CalendarStartDayOfWeekSetting,
                        CalendarStartDayOfWeekSettingLoadException
                > = UseCaseResult.Success(setting ?: defaultSettingValue)
                result
            }.catch { cause: Throwable ->
                val result =
                    when (cause) {
                        is UnknownException -> {
                            Log.w(
                                logTag,
                                "${logMsg}失敗_原因不明 (データ層)、" +
                                        "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                cause
                            )
                            UseCaseResult.Failure(
                                CalendarStartDayOfWeekSettingLoadException
                                    .Unknown(defaultSettingValue, cause),
                            )
                        }
                        is DomainException -> {
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
                        else -> {
                            // 予期せぬ例外を捕捉。詳細はクラスのKDocを参照。
                            Log.w(
                                logTag,
                                "${logMsg}失敗_原因不明、" +
                                        "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                cause
                            )
                            UseCaseResult.Failure(
                                CalendarStartDayOfWeekSettingLoadException
                                    .Unknown(defaultSettingValue, cause),
                            )
                        }
                    }
                emit(result)
            }
    }
}
