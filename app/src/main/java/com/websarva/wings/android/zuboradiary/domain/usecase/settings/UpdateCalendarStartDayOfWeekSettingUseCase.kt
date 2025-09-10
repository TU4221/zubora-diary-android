package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.DayOfWeek

/**
 * カレンダーの週の開始曜日設定を更新するユースケース。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class UpdateCalendarStartDayOfWeekSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "カレンダー開始曜日設定更新_"

    /**
     * ユースケースを実行し、指定された曜日をカレンダーの週の開始曜日として更新する。
     *
     * @param dayOfWeek 更新する週の開始曜日。
     * @return 更新処理が成功した場合は [UseCaseResult.Success] を返す。
     *   更新処理中に [DataStorageException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        dayOfWeek: DayOfWeek
    ): UseCaseResult<Unit, CalendarStartDayOfWeekSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (曜日: $dayOfWeek)")

        try {
            val preferenceValue = CalendarStartDayOfWeekSetting(dayOfWeek)
            settingsRepository.updateCalendarStartDayOfWeekPreference(preferenceValue)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定更新処理エラー", e)
            return UseCaseResult.Failure(
                CalendarStartDayOfWeekSettingUpdateException.UpdateFailure(dayOfWeek, e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
