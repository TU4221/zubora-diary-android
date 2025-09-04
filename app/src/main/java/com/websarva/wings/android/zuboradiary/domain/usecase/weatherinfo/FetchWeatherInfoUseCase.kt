package com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.FetchWeatherInfoUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.exception.weather.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 指定された日付の天気情報を取得するユースケース。
 *
 * 位置情報権限の確認、取得可能日の確認を行った上で、天気情報を取得する。
 *
 * @property weatherInfoRepository 天気情報関連の操作を行うリポジトリ。
 * @property canFetchWeatherInfoUseCase 指定された日付の天気情報を取得可能かどうかを判断するユースケース。
 */
internal class FetchWeatherInfoUseCase(
    private val weatherInfoRepository: WeatherInfoRepository,
    private val canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "天気情報取得_"

    /**
     * ユースケースを実行し、指定された日付の天気情報を取得する。
     *
     * @param isGranted 位置情報権限が付与されているかどうか。
     * @param date 天気情報を取得する日付。
     * @return 取得した天気情報 ([Weather]) を [UseCaseResult.Success] に格納して返す。
     *   処理中にエラーが発生した場合は、対応する [FetchWeatherInfoUseCaseException] を
     *   [UseCaseResult.Failure] に格納して返す。
     */
    suspend operator fun invoke(isGranted: Boolean, date: LocalDate): DefaultUseCaseResult<Weather> {
        Log.i(logTag, "${logMsg}開始 (権限付与: $isGranted, 日付: $date)")

        if (!canFetchWeatherInfoUseCase(date).value) {
            val exception = FetchWeatherInfoUseCaseException.WeatherInfoDateOutOfRange(date)
            Log.w(logTag, "${logMsg}失敗_取得対象外の日付 (日付: $date)", exception)
            return UseCaseResult.Failure(exception)
        }

        if (!isGranted) {
            val exception = FetchWeatherInfoUseCaseException.LocationPermissionNotGranted()
            Log.w(logTag, "${logMsg}失敗_位置情報権限未取得", exception)
            return UseCaseResult.Failure(exception)
        }

        try {
            val weather = weatherInfoRepository.fetchWeatherInfo(date)
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(weather)
        } catch (e: WeatherInfoFetchException) {
            val re = when (e) {
                is WeatherInfoFetchException.AccessLocationFailure -> {
                    Log.e(logTag, "${logMsg}失敗_位置情報アクセスエラー", e)
                    FetchWeatherInfoUseCaseException.LocationAccessFailure(e)
                }
                is WeatherInfoFetchException.ApiAccessFailure -> {
                    Log.e(logTag, "${logMsg}失敗_APIアクセスエラー", e)
                    FetchWeatherInfoUseCaseException.WeatherInfoFetchFailure(e)
                }
                is WeatherInfoFetchException.DateOutOfRange -> {
                    // MEMO:このパスは通常 canFetchWeatherInfoUseCase で防がれる
                    Log.e(logTag, "${logMsg}失敗_取得対象外の日付 (日付: $date)", e)
                    FetchWeatherInfoUseCaseException.WeatherInfoDateOutOfRange(date, e)
                }
            }
            return UseCaseResult.Failure(re)
        }
    }
}
