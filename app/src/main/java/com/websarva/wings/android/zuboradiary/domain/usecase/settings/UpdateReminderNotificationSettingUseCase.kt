package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.exception.ReminderNotificationCancelException
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.exception.ReminderNotificationRegisterException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ReminderNotificationSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ReminderNotificationSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.CancelReminderNotificationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.RegisterReminderNotificationUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalTime

/**
 * リマインダー通知設定を更新するユースケース。
 *
 * 設定の有効/無効に応じて、通知の登録またはキャンセルも行う。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 * @property loadReminderNotificationSettingUseCase リマインダー通知設定を読み込むユースケース。
 * @property registerReminderNotificationUseCase リマインダー通知を登録するユースケース。
 * @property cancelReminderNotificationUseCase リマインダー通知をキャンセルするユースケース。
 */
internal class UpdateReminderNotificationSettingUseCase(
    private val settingsRepository: SettingsRepository,
    private val loadReminderNotificationSettingUseCase: LoadReminderNotificationSettingUseCase,
    private val registerReminderNotificationUseCase: RegisterReminderNotificationUseCase,
    private val cancelReminderNotificationUseCase: CancelReminderNotificationUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "リマインダー通知設定更新_"

    /**
     * ユースケースを実行し、リマインダー通知設定を更新する。
     *
     * 設定が有効な場合は、指定された時刻で通知を登録する。
     * 設定が無効な場合は、既存の通知をキャンセルする。
     *
     * @param setting 更新する設定 [ReminderNotificationSetting] 。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [ReminderNotificationSettingUpdateException] を格納して返す。
     */
    suspend operator fun invoke(
        setting: ReminderNotificationSetting
    ): UseCaseResult<Unit, ReminderNotificationSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (設定値: $setting)")

        try {
            updateReminderNotification(setting)
        } catch (e: ReminderNotificationSettingUpdateException) {
            when (e) {
                is ReminderNotificationSettingUpdateException.BackupFailure -> {
                    Log.e(logTag, "${logMsg}失敗_設定読込エラー", e)
                }
                is ReminderNotificationSettingUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_設定更新エラー", e)
                }
                is ReminderNotificationSettingUpdateException.SchedulingRegisterFailure -> {
                    Log.e(logTag, "${logMsg}失敗_通知登録エラー、設定ロールバック成功", e)
                }
                is ReminderNotificationSettingUpdateException.SchedulingCancelFailure -> {
                    Log.e(logTag, "${logMsg}失敗_通知キャンセルエラー、設定ロールバック成功", e)
                }
                is ReminderNotificationSettingUpdateException.RollbackFailure -> {
                    when (setting) {
                        is ReminderNotificationSetting.Enabled -> {
                            Log.e(logTag, "${logMsg}失敗_通知登録エラー、設定ロールバック失敗", e)
                        }
                        ReminderNotificationSetting.Disabled -> {
                            Log.e(logTag, "${logMsg}失敗_通知キャンセルエラー、設定ロールバック失敗", e)
                        }
                    }
                }
            }
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    // TODO:細分化。可読性が悪い。
    /**
     * リマインダー通知設定を更新。
     *
     * リマインダー通知設定を有効とした場合、通知を登録する。無効とした場合、通知をキャンセルする。
     *
     * 通知の登録、キャンセルに失敗した場合、元の設定値に戻すようにロールバック処理が行われる。
     *
     * @throws ReminderNotificationSettingUpdateException 設定の更新に失敗した場合。
     * */
    private suspend fun updateReminderNotification(
        settingValue: ReminderNotificationSetting,
    ) {
        val backupSettingValue =
            try {
                fetchCurrentReminderNotificationSetting()
            } catch (e: ReminderNotificationSettingLoadException) {
                throw ReminderNotificationSettingUpdateException.BackupFailure(e)
            }

        try {
            settingsRepository.updateReminderNotificationSetting(settingValue)
        } catch (e: DataStorageException) {
            throw ReminderNotificationSettingUpdateException.UpdateFailure(
                settingValue,
                e
            )
        }

        try {
            when (settingValue) {
                is ReminderNotificationSetting.Enabled -> {
                    registerReminderNotification(settingValue.notificationTime)
                }
                ReminderNotificationSetting.Disabled -> {
                    cancelReminderNotification()
                }
            }
        } catch (e: UseCaseException) {
            rollbackReminderNotification(backupSettingValue)

            throw when (e) {
                is ReminderNotificationRegisterException -> {
                    ReminderNotificationSettingUpdateException.SchedulingRegisterFailure(e)
                }
                is ReminderNotificationCancelException -> {
                    ReminderNotificationSettingUpdateException.SchedulingCancelFailure(e)
                }
                else -> IllegalStateException()
            }
        }
    }

    /**
     * 現在のリマインダー通知設定を読み込む。
     *
     * @return 現在のリマインダー通知設定。
     * @throws ReminderNotificationSettingLoadException 設定の読み込みに失敗した場合。
     */
    private suspend fun fetchCurrentReminderNotificationSetting(): ReminderNotificationSetting {
        return withContext(Dispatchers.IO) {
            loadReminderNotificationSettingUseCase()
                .map {
                    when (it) {
                        is UseCaseResult.Success -> {
                            it.value
                        }
                        is UseCaseResult.Failure -> {
                            throw it.exception
                        }
                    }
                }.first()
        }
    }

    /**
     * 指定された時刻でリマインダー通知を登録する。
     *
     * @param notificationTime 通知時刻。
     * @throws ReminderNotificationRegisterException 通知の登録に失敗した場合。
     */
    private fun registerReminderNotification(notificationTime: LocalTime) {
        when (val result = registerReminderNotificationUseCase(notificationTime)) {
            is UseCaseResult.Success -> {
                // 処理不要
            }
            is UseCaseResult.Failure -> {
                throw ReminderNotificationSettingUpdateException.SchedulingRegisterFailure(result.exception)
            }
        }
    }

    /**
     * リマインダー通知をキャンセルする。
     *
     * @throws ReminderNotificationCancelException 通知のキャンセルに失敗した場合。
     */
    private fun cancelReminderNotification() {
        when (val result = cancelReminderNotificationUseCase()) {
            is UseCaseResult.Success -> {
                // 処理不要
            }
            is UseCaseResult.Failure -> {
                throw ReminderNotificationSettingUpdateException.SchedulingCancelFailure(result.exception)
            }
        }
    }

    /**
     * リマインダー通知設定を指定された値にロールバックする。
     *
     * 通知の登録やキャンセル処理に失敗した際に、設定を以前の状態に戻すために使用される。
     *
     * @param backupSettingValue ロールバック先のリマインダー通知設定値。
     * @throws ReminderNotificationSettingUpdateException.RollbackFailure ロールバック処理に失敗した場合。
     */
    private suspend fun rollbackReminderNotification(
        backupSettingValue: ReminderNotificationSetting
    ) {
        try {
            settingsRepository.updateReminderNotificationSetting(backupSettingValue)
        } catch (e: DataStorageException) {
            throw ReminderNotificationSettingUpdateException.RollbackFailure(
                backupSettingValue,
                e
            )
        }
    }
}
