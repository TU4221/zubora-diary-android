package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ReminderNotificationSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ReminderNotificationSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RollbackException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.SchedulingException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * リマインダー通知設定を更新するユースケース。
 *
 * 設定の有効/無効に応じて、通知の登録またはキャンセルも行う。
 * 通知の登録、キャンセルに失敗した場合、元の設定値に戻すようにロールバック処理が行われる。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 * @property schedulingRepository スケジューリング関連の操作を行うリポジトリ。
 * @property loadReminderNotificationSettingUseCase リマインダー通知設定を読み込むユースケース。
 */
internal class UpdateReminderNotificationSettingUseCase(
    private val settingsRepository: SettingsRepository,
    private val schedulingRepository: SchedulingRepository,
    private val loadReminderNotificationSettingUseCase: LoadReminderNotificationSettingUseCase
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
            val backupSetting = fetchCurrentReminderNotificationSetting(setting)
            updateReminderNotificationSetting(setting)
            updateReminderScheduling(setting, backupSetting)
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(Unit)
        } catch (e: ReminderNotificationSettingUpdateException) {
            return UseCaseResult.Failure(e)
        }
    }

    /**
     * 現在のリマインダー通知設定を読み込む。
     *
     * @param setting 現在の設定。例外発生時の情報として使用。
     * @return 現在のリマインダー通知設定。
     * @throws ReminderNotificationSettingUpdateException.SettingUpdateFailure 設定読み込みに失敗した場合。
     */
    private suspend fun fetchCurrentReminderNotificationSetting(
        setting: ReminderNotificationSetting
    ): ReminderNotificationSetting {
        return withContext(Dispatchers.IO) {
            try {
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
            } catch (e: ReminderNotificationSettingLoadException) {
                Log.e(logTag, "${logMsg}失敗_設定読込(バックアップ用)エラー", e)
                throw ReminderNotificationSettingUpdateException.SettingUpdateFailure(setting, e)
            }
        }
    }

    /**
     * リマインダー通知設定を更新する。
     *
     * @param setting 更新するリマインダー通知設定。
     * @throws ReminderNotificationSettingUpdateException.SettingUpdateFailure 設定の更新に失敗した場合。
     */
    private suspend fun updateReminderNotificationSetting(setting: ReminderNotificationSetting) {
        try {
            settingsRepository.updateReminderNotificationSetting(setting)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定更新エラー", e)
            throw ReminderNotificationSettingUpdateException.SettingUpdateFailure(
                setting,
                e
            )
        }
    }

    /**
     * リマインダー通知のスケジュールを登録またはキャンセルする。
     *
     * [updateSetting]が有効な場合は通知をスケジュールし、無効な場合はキャンセルする。
     *
     * @param updateSetting 更新後のリマインダー通知設定。
     * @param backupSetting ロールバック用の更新前のリマインダー通知設定。
     */
    private suspend fun updateReminderScheduling(
        updateSetting: ReminderNotificationSetting,
        backupSetting: ReminderNotificationSetting
    ) {
        when (updateSetting) {
            is ReminderNotificationSetting.Enabled -> {
                executeSchedulingWithRollback(updateSetting, backupSetting) {
                    schedulingRepository.registerReminderNotification(
                        updateSetting.notificationTime
                    )
                }
            }
            ReminderNotificationSetting.Disabled -> {
                executeSchedulingWithRollback(updateSetting, backupSetting) {
                    schedulingRepository.cancelReminderNotification()
                }
            }
        }
    }

    /**
     * スケジューリング処理(登録または解除)を実行し、失敗時には設定をロールバックする。
     *
     * @param updateSetting 更新後のリマインダー通知設定。
     * @param backupSetting ロールバック用の更新前のリマインダー通知設定。
     * @param processScheduling 実行するスケジューリング処理（通知登録または解除）。
     * @throws ReminderNotificationSettingUpdateException.SchedulingUpdateFailure スケジューリング処理に失敗した場合。
     */
    private suspend fun executeSchedulingWithRollback(
        updateSetting: ReminderNotificationSetting,
        backupSetting: ReminderNotificationSetting,
        processScheduling: suspend (updateSetting: ReminderNotificationSetting) -> Unit
    ) {
        try {
            processScheduling(updateSetting)
        } catch (e: SchedulingException) {
            try {
                settingsRepository.updateReminderNotificationSetting(backupSetting)
            } catch(de: DataStorageException) {
                Log.w(logTag, "${logMsg}_ロールバックエラー", e)
                val isEnabledString = if (backupSetting.isEnabled) "有効" else "無効"
                e.addSuppressed(
                    RollbackException(
                        "通知${isEnabledString}化処理中のエラー後、設定ロールバックに失敗",
                        de
                    )
                )
            }
            Log.e(logTag, "${logMsg}失敗_スケジューリング更新エラー", e)
            throw ReminderNotificationSettingUpdateException
                .SchedulingUpdateFailure(updateSetting, e)
        }
    }
}
