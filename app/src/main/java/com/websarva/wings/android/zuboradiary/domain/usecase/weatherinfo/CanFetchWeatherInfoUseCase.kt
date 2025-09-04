package com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 指定された日付の天気情報を取得可能かどうかを判断するユースケース。
 *
 * @property weatherInfoRepository 天気情報関連の操作を行うリポジトリ。
 */
internal class CanFetchWeatherInfoUseCase(
    private val weatherInfoRepository: WeatherInfoRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "天気情報取得可能日確認_"

    /**
     * ユースケースを実行し、指定された日付の天気情報を取得可能かどうかを返す。
     *
     * @param date 天気情報の取得可否を確認する日付。
     * @return 天気情報を取得可能な場合は `true`、そうでない場合は `false` を
     *   [UseCaseResult.Success] に格納して返す。
     *   このユースケースは常に成功するため、[UseCaseResult.Failure] を返すことはない。
     */
    operator fun invoke(date: LocalDate): UseCaseResult.Success<Boolean> {
        Log.i(logTag, "${logMsg}開始 (日付: $date)")
        val canFetch = weatherInfoRepository.canFetchWeatherInfo(date)
        Log.i(logTag, "${logMsg}完了 (結果: $canFetch)")
        return UseCaseResult.Success(canFetch)
    }
}
