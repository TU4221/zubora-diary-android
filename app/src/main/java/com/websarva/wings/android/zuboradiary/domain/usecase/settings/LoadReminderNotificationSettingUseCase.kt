package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ReminderNotificationSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * リマインダー通知設定を読み込むユースケース。
 *
 * 設定の読み込み結果を [Flow] として提供する。
 *
 * このユースケースは、アプリの動作に不可欠な設定値を取得するため、
 * データソース層からいかなる種類の例外（RuntimeExceptionなどを含む）がスローされた場合でも、
 * ViewModelに例外を再スローせず、必ずフォールバック値を含む [UseCaseResult] を返す。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class LoadReminderNotificationSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logMsg = "リマインダー通知設定読込_"

    /**
     * ユースケースを実行し、リマインダー通知設定の読み込み結果を [Flow] として返す。
     *
     * @return 読み込み結果を [UseCaseResult] へ [Flow] 内部でラップして返す。
     *   読み込みに成功した場合は[UseCaseResult.Success] に
     *   リマインダー通知設定( [ReminderNotificationSetting] )を格納して返す。
     *   失敗した場合は、[UseCaseResult.Failure] にフォールバック値を格納した
     *   [ReminderNotificationSettingLoadException] を格納して返す。
     */
    operator fun invoke(): Flow<UseCaseResult<ReminderNotificationSetting, ReminderNotificationSettingLoadException>> {
        Log.i(logTag, "${logMsg}開始")

        val defaultSettingValue = ReminderNotificationSetting.default()
        return settingsRepository
            .loadReminderNotificationSetting()
            .map { setting ->
                Log.d(
                    logTag,
                    "${logMsg}読込成功 (${setting?.let { "設定値: $it" } ?: "未設定"})"
                )
                val result: UseCaseResult<ReminderNotificationSetting, ReminderNotificationSettingLoadException> =
                    UseCaseResult.Success(setting ?: defaultSettingValue)
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
                                ReminderNotificationSettingLoadException
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
                                ReminderNotificationSettingLoadException
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
                                ReminderNotificationSettingLoadException
                                    .Unknown(defaultSettingValue, cause),
                            )
                        }
                    }
                emit(result)
            }
    }
}
