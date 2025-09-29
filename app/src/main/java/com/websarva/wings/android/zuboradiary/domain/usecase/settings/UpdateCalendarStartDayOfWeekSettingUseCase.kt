package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

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
     * @param setting 更新する設定 [CalendarStartDayOfWeekSetting] 。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [CalendarStartDayOfWeekSettingUpdateException] を格納して返す。
     */
    suspend operator fun invoke(
        setting: CalendarStartDayOfWeekSetting
    ): UseCaseResult<Unit, CalendarStartDayOfWeekSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (設定値: $setting)")

        return try {
            settingsRepository.updateCalendarStartDayOfWeekSetting(setting)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定更新エラー", e)
            UseCaseResult.Failure(
                CalendarStartDayOfWeekSettingUpdateException.UpdateFailure(setting, e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                CalendarStartDayOfWeekSettingUpdateException.Unknown(e)
            )
        }
    }
}
