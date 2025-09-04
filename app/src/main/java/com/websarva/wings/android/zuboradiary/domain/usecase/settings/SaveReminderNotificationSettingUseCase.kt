package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationCancellationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationRegistrationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingRollbackFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.CancelReminderNotificationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.RegisterReminderNotificationUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalTime

/**
 * リマインダー通知設定を保存するユースケース。
 *
 * 設定の有効/無効に応じて、通知の登録またはキャンセルも行う。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 * @property loadReminderNotificationSettingUseCase リマインダー通知設定を読み込むユースケース。
 * @property registerReminderNotificationUseCase リマインダー通知を登録するユースケース。
 * @property cancelReminderNotificationUseCase リマインダー通知をキャンセルするユースケース。
 */
internal class SaveReminderNotificationSettingUseCase(
    private val settingsRepository: SettingsRepository,
    private val loadReminderNotificationSettingUseCase: LoadReminderNotificationSettingUseCase,
    private val registerReminderNotificationUseCase: RegisterReminderNotificationUseCase,
    private val cancelReminderNotificationUseCase: CancelReminderNotificationUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "リマインダー通知設定保存_"

    /**
     * ユースケースを実行し、リマインダー通知設定を保存する。
     *
     * 設定が有効な場合は、指定された時刻で通知を登録する。
     * 設定が無効な場合は、既存の通知をキャンセルする。
     *
     * @param isChecked リマインダー通知を有効にする場合は `true`、無効にする場合は `false`。
     * @param notificationTime 通知時刻。`isChecked` が `true` の場合に必須。
     * @return 保存処理および通知の登録/キャンセル処理が成功した場合は [UseCaseResult.Success] を返す。
     *   処理中に [DomainException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        isChecked: Boolean,
        notificationTime: LocalTime? = null
    ): DefaultUseCaseResult<Unit> {
        Log.i(logTag, "${logMsg}開始 (有効: $isChecked, 通知時刻: ${notificationTime ?: "未指定"})")

        if (isChecked) {
            requireNotNull(notificationTime) { "${logMsg}不正引数_リマインダー通知を有効にする場合、通知時刻は必須 (通知時刻: null)" }

            try {
                saveReminderNotificationValid(notificationTime)
            } catch (e: ReminderNotificationSettingUpdateFailureException) {
                Log.e(logTag, "${logMsg}失敗_設定更新エラー", e)
                return UseCaseResult.Failure(e)
            } catch (e: ReminderNotificationRegistrationFailureException) {
                Log.e(logTag, "${logMsg}失敗_通知登録エラー、設定ロールバック成功", e)
                return UseCaseResult.Failure(e)
            } catch (e: ReminderNotificationSettingRollbackFailureException) {
                Log.e(logTag, "${logMsg}失敗_通知登録エラー、設定ロールバック失敗", e)
                return UseCaseResult.Failure(e)
            }

        } else {
            try {
                saveReminderNotificationInvalid()
            } catch (e: UserSettingsLoadException) {
                Log.e(logTag, "${logMsg}失敗_設定読込エラー", e)
                return UseCaseResult.Failure(e)
            } catch (e: ReminderNotificationSettingUpdateFailureException) {
                Log.e(logTag, "${logMsg}失敗_設定更新エラー", e)
                return UseCaseResult.Failure(e)
            } catch (e: ReminderNotificationCancellationFailureException) {
                Log.e(logTag, "${logMsg}失敗_通知登録エラー、設定ロールバック成功", e)
                return UseCaseResult.Failure(e)
            } catch (e: ReminderNotificationSettingRollbackFailureException) {
                Log.e(logTag, "${logMsg}失敗_通知登録エラー、設定ロールバック失敗", e)
                return UseCaseResult.Failure(e)
            }
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    // TODO:saveReminderNotificationInvalid()と統一
    /**
     * リマインダー通知設定を有効として保存し、通知を登録する。
     *
     * @param notificationTime 通知時刻。
     * @throws ReminderNotificationSettingUpdateFailureException 設定の更新に失敗した場合。
     * @throws ReminderNotificationRegistrationFailureException 通知の登録に失敗した場合。
     */
    private suspend fun saveReminderNotificationValid(notificationTime: LocalTime) {
        val preferenceValue = ReminderNotificationSetting.Enabled(notificationTime)
        try {
            settingsRepository.saveReminderNotificationPreference(preferenceValue)
            registerReminderNotification(notificationTime)
        } catch (e: ReminderNotificationSettingUpdateFailureException) {
            throw e
        } catch (e: ReminderNotificationRegistrationFailureException) {
            try {
                rollbackReminderNotification(preferenceValue)
            } catch (e: ReminderNotificationSettingRollbackFailureException) {
                throw e
            }
            throw e
        }
    }

    /**
     * 指定された時刻でリマインダー通知を登録する。
     *
     * @param notificationTime 通知時刻。
     * @throws ReminderNotificationRegistrationFailureException 通知の登録に失敗した場合。
     */
    private fun registerReminderNotification(notificationTime: LocalTime) {
        when (val result = registerReminderNotificationUseCase(notificationTime)) {
            is UseCaseResult.Success -> {
                // 処理不要
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    /**
     * リマインダー通知設定を無効として保存し、通知をキャンセルする。
     *
     * @throws ReminderNotificationSettingUpdateFailureException 設定の更新に失敗した場合。
     * @throws ReminderNotificationCancellationFailureException 通知のキャンセルに失敗した場合。
     * @throws UserSettingsLoadException 現在の設定の読み込みに失敗した場合（ロールバック用）。
     */
    private suspend fun saveReminderNotificationInvalid() {
        val backupSettingValue = fetchCurrentReminderNotificationSetting()
        try {
            val preferenceValue = ReminderNotificationSetting.Disabled
            settingsRepository.saveReminderNotificationPreference(preferenceValue)
            cancelReminderNotification()
        } catch (e: ReminderNotificationSettingUpdateFailureException) {
            throw e
        } catch (e: ReminderNotificationCancellationFailureException) {
            try {
                rollbackReminderNotification(backupSettingValue)
            } catch (e: ReminderNotificationSettingRollbackFailureException) {
                throw e
            }
            throw e
        }
    }

    /**
     * 現在のリマインダー通知設定を読み込む。
     *
     * @return 現在のリマインダー通知設定。
     * @throws UserSettingsLoadException 設定の読み込みに失敗した場合。
     */
    private suspend fun fetchCurrentReminderNotificationSetting(): ReminderNotificationSetting {
        return withContext(Dispatchers.IO) {
            loadReminderNotificationSettingUseCase().value
                .map { result: UserSettingResult<ReminderNotificationSetting> ->
                    when (result) {
                        is UserSettingResult.Success -> {
                            result.setting
                        }
                        is UserSettingResult.Failure -> {
                            throw result.exception
                        }
                    }
                }.first()
        }
    }

    /**
     * リマインダー通知をキャンセルする。
     *
     * @throws ReminderNotificationCancellationFailureException 通知のキャンセルに失敗した場合。
     */
    private fun cancelReminderNotification() {
        when (val result = cancelReminderNotificationUseCase()) {
            is UseCaseResult.Success -> {
                // 処理不要
            }
            is UseCaseResult.Failure -> {
                throw result.exception
            }
        }
    }

    /**
     * リマインダー通知設定を指定された値にロールバックする。
     *
     * 通知の登録やキャンセル処理に失敗した際に、設定を以前の状態に戻すために使用される。
     *
     * @param backupSettingValue ロールバック先のリマインダー通知設定値。
     * @throws ReminderNotificationSettingRollbackFailureException ロールバック処理に失敗した場合。
     */
    private suspend fun rollbackReminderNotification(
        backupSettingValue: ReminderNotificationSetting
    ) {
        try {
            settingsRepository.saveReminderNotificationPreference(backupSettingValue)
            Log.i(logTag, "${logMsg}設定ロールバック完了")
        } catch (e: ReminderNotificationSettingUpdateFailureException) {
            Log.e(logTag, "${logMsg}設定ロールバック失敗", e)
            val backupNotificationTime =
                when (backupSettingValue) {
                    is ReminderNotificationSetting.Enabled -> backupSettingValue.notificationTime
                    ReminderNotificationSetting.Disabled -> null
                }
            throw ReminderNotificationSettingRollbackFailureException(
                backupSettingValue.isEnabled,
                backupNotificationTime,
                e
            )
        }
    }
}
