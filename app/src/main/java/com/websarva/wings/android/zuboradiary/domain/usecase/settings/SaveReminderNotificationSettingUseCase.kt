package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.repository.WorkerRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.CancelReminderNotificationFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.RegisterReminderNotificationFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateReminderNotificationSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalTime

internal class SaveReminderNotificationSettingUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workerRepository: WorkerRepository
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
        UpdateReminderNotificationSettingFailedException::class,
        RegisterReminderNotificationFailedException::class
    )
    private suspend fun saveReminderNotificationValid(notificationTime: LocalTime) {
        try {
            val preferenceValue = ReminderNotificationSetting.Enabled(notificationTime)
            userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
            workerRepository.registerReminderNotificationWorker(notificationTime)
        } catch (e: UpdateReminderNotificationSettingFailedException) {
            throw e
        } catch (e: RegisterReminderNotificationFailedException) {
            try {
                val preferenceValue = ReminderNotificationSetting.Disabled
                userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
            } catch (e: UpdateReminderNotificationSettingFailedException) {
                throw e
            }
            throw e
        }
    }

    @Throws(
        UpdateReminderNotificationSettingFailedException::class,
        CancelReminderNotificationFailedException::class,
        UserSettingsException::class
    )
    private suspend fun saveReminderNotificationInvalid() {
        val backupSettingValue = fetchCurrentReminderNotificationSetting()
        try {
            val preferenceValue = ReminderNotificationSetting.Disabled
            userPreferencesRepository.saveReminderNotificationPreference(preferenceValue)
            workerRepository.cancelReminderNotificationWorker()
        } catch (e: UpdateReminderNotificationSettingFailedException) {
            throw e
        } catch (e: CancelReminderNotificationFailedException) {
            try {
                userPreferencesRepository.saveReminderNotificationPreference(backupSettingValue)
            } catch (e: UpdateReminderNotificationSettingFailedException) {
                throw e
            }
            throw e
        }
    }

    @Throws(UserSettingsException::class)
    private suspend fun fetchCurrentReminderNotificationSetting(): ReminderNotificationSetting {
        return withContext(Dispatchers.IO) {
            userPreferencesRepository
                .fetchReminderNotificationPreference()
                .map { value: UserSettingDataSourceResult<ReminderNotificationSetting> ->
                    when (value) {
                        is UserSettingDataSourceResult.Success -> value.setting
                        is UserSettingDataSourceResult.Failure -> throw value.exception
                    }
                }.first()
        }
    }
}
