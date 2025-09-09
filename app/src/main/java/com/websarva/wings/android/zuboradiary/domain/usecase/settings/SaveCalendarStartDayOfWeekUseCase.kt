package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.CalendarStartDayOfWeekSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.DayOfWeek

// TODO:クラス名修正。SaveCalendarStartDayOf`setting`WeekUseCase(diメソッドも)
/**
 * カレンダーの週の開始曜日設定を保存するユースケース。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class SaveCalendarStartDayOfWeekUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "カレンダー開始曜日設定保存_"

    /**
     * ユースケースを実行し、指定された曜日をカレンダーの週の開始曜日として保存する。
     *
     * @param dayOfWeek 保存する週の開始曜日。
     * @return 保存処理が成功した場合は [UseCaseResult.Success] を返す。
     *   保存処理中に [DataStorageException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        dayOfWeek: DayOfWeek
    ): UseCaseResult<Unit, CalendarStartDayOfWeekSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (曜日: $dayOfWeek)")

        try {
            val preferenceValue = CalendarStartDayOfWeekSetting(dayOfWeek)
            settingsRepository.saveCalendarStartDayOfWeekPreference(preferenceValue)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定保存処理エラー", e)
            return UseCaseResult.Failure(
                CalendarStartDayOfWeekSettingUpdateException.UpdateFailure(dayOfWeek, e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
