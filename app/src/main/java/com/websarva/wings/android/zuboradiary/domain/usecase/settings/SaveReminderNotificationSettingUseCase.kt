package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.data.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.data.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationCancellationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationRegistrationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalTime

internal class SaveReminderNotificationSettingUseCase(
    private val settingsRepository: SettingsRepository,
    private val schedulingRepository: SchedulingRepository,
    private val loadReminderNotificationSettingUseCase: LoadReminderNotificationSettingUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        isChecked: Boolean,
        notificationTime: LocalTime? = null
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "リマインダー通知設定保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            if (isChecked) {
                requireNotNull(notificationTime)

                saveReminderNotificationValid(notificationTime)
            } else {
                saveReminderNotificationInvalid()
            }
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗")
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    @Throws(
        ReminderNotificationSettingUpdateFailureException::class,
        ReminderNotificationRegistrationFailureException::class
    )
    private suspend fun saveReminderNotificationValid(notificationTime: LocalTime) {
        try {
            val preferenceValue = ReminderNotificationSetting.Enabled(notificationTime)
            settingsRepository.saveReminderNotificationPreference(preferenceValue)
            schedulingRepository.registerReminderNotificationWorker(notificationTime)
        } catch (e: ReminderNotificationSettingUpdateFailureException) {
            throw e
        } catch (e: ReminderNotificationRegistrationFailureException) {
            try {
                val preferenceValue = ReminderNotificationSetting.Disabled
                settingsRepository.saveReminderNotificationPreference(preferenceValue)
            } catch (e: ReminderNotificationSettingUpdateFailureException) {
                throw e
            }
            throw e
        }
    }

    @Throws(
        ReminderNotificationSettingUpdateFailureException::class,
        ReminderNotificationCancellationFailureException::class,
        UserSettingsException::class
    )
    private suspend fun saveReminderNotificationInvalid() {
        val backupSettingValue = fetchCurrentReminderNotificationSetting()
        try {
            val preferenceValue = ReminderNotificationSetting.Disabled
            settingsRepository.saveReminderNotificationPreference(preferenceValue)
            schedulingRepository.cancelReminderNotificationWorker()
        } catch (e: ReminderNotificationSettingUpdateFailureException) {
            throw e
        } catch (e: ReminderNotificationCancellationFailureException) {
            try {
                settingsRepository.saveReminderNotificationPreference(backupSettingValue)
            } catch (e: ReminderNotificationSettingUpdateFailureException) {
                throw e
            }
            throw e
        }
    }

    @Throws(UserSettingsException::class)
    private suspend fun fetchCurrentReminderNotificationSetting(): ReminderNotificationSetting {
        return withContext(Dispatchers.IO) {
            loadReminderNotificationSettingUseCase().value
                .map { result: UserSettingResult<ReminderNotificationSetting> ->
                    when (result) {
                        is UserSettingResult.Success -> result.setting
                        is UserSettingResult.Failure -> throw result.exception
                    }
                }.first()
        }
    }
}
