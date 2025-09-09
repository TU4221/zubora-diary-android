package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ReminderNotificationSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NotFoundException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * リマインダー通知設定を読み込むユースケース。
 *
 * 設定の読み込み結果を [Flow] として提供する。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class LoadReminderNotificationSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "リマインダー通知設定読込_"

    /**
     * ユースケースを実行し、リマインダー通知設定の読み込み結果を [Flow] として返す。
     *
     * リポジトリから設定値を読み込み、その結果を [UserSettingResult] に変換する。
     * - リポジトリからの読み込み成功時: [UserSettingResult.Success] を発行。
     * - リポジトリからの読み込み失敗時 ([DataStorageException]、 [NotFoundException] が発生した場合):
     *     - [DataStorageException]: [UserSettingResult.Failure] を発行し、デフォルト値をフォールバックとして提供する。
     *     - [NotFoundException]: [UserSettingResult.Success] を発行し、デフォルト値を設定値として提供する。
     *
     * @return リマインダー通知設定の読み込み結果を [UserSettingResult] へ [Flow] でラップし、
     *   [UseCaseResult.Success] に格納して返す。このユースケースは、常に [UseCaseResult.Success] を返す。
     */
    operator fun invoke(): UseCaseResult.Success<Flow<UserSettingResult<ReminderNotificationSetting>>> {
        Log.i(logTag, "${logMsg}開始")

        val flow =
            settingsRepository
                .loadReminderNotificationPreference()
                .map { setting: ReminderNotificationSetting ->
                    Log.d(
                        logTag,
                        "${logMsg}読込成功 (設定値: ${setting})"
                    )
                    val result: UserSettingResult<ReminderNotificationSetting> =
                        UserSettingResult.Success(setting)
                    result
                }.catch { cause: Throwable ->
                    val defaultSettingValue = ReminderNotificationSetting.Disabled
                    val userSettingResult =
                        when (cause) {
                            is DataStorageException -> {
                                Log.w(
                                    logTag,
                                    "${logMsg}失敗_アクセス失敗、" +
                                            "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                    cause
                                )
                                UserSettingResult.Failure(
                                    ReminderNotificationSettingLoadException.LoadFailure(cause),
                                    defaultSettingValue
                                )
                            }
                            is NotFoundException -> {
                                Log.i(
                                    logTag,
                                    "${logMsg}失敗_データ未発見、" +
                                            "デフォルト値を設定値として使用 (デフォルト値: $defaultSettingValue)"
                                )
                                UserSettingResult.Success(defaultSettingValue)
                            }
                            else -> throw cause
                        }
                    emit(userSettingResult)
                }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(flow)
    }
}
