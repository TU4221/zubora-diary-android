package com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.exception.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.LocationException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NetworkConnectionException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.PermissionException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

/**
 * 指定された日付の天気情報を取得するユースケース。
 *
 * 位置情報の取得、取得可能日の確認を行った上で、天気情報を取得する。
 *
 * @property weatherInfoRepository 天気情報関連の操作を行うリポジトリ。
 * @property locationRepository 位置情報関連の操作を行うリポジトリ。
 */
internal class FetchWeatherInfoUseCase(
    private val weatherInfoRepository: WeatherInfoRepository,
    private val locationRepository: LocationRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "天気情報取得_"

    /**
     * ユースケースを実行し、指定された日付の天気情報を取得する。
     *
     * @param date 天気情報を取得する日付。
     * @return 処理に成功した場合は [UseCaseResult.Success] に [Weather] を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [WeatherInfoFetchException] を格納して返す。
     */
    suspend operator fun invoke(date: LocalDate): UseCaseResult<Weather, WeatherInfoFetchException> {
        Log.i(logTag, "${logMsg}開始 (日付: $date)")

        if (!weatherInfoRepository.canFetchWeatherInfo(date)) {
            val exception = WeatherInfoFetchException.DateOutOfRange(date)
            Log.w(logTag, "${logMsg}失敗_取得対象外の日付 (日付: $date)", exception)
            return UseCaseResult.Failure(exception)
        }

        val location =
            try {
                 locationRepository.fetchCurrentLocation()
            } catch (e: PermissionException) {
                Log.w(logTag, "${logMsg}失敗_位置情報権限未取得", e)
                return UseCaseResult.Failure(
                    WeatherInfoFetchException.LocationPermissionNotGranted(e)
                )
            } catch (e: LocationException) {
                Log.e(logTag, "${logMsg}失敗_位置情報アクセスエラー", e)
                return UseCaseResult.Failure(
                    WeatherInfoFetchException.LocationAccessFailure(e)
                )
            }

        return try {
            val weather = weatherInfoRepository.fetchWeatherInfo(date, location)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(weather)
        } catch (e: NetworkConnectionException) {
            Log.e(logTag, "${logMsg}失敗_APIアクセスエラー", e)
            UseCaseResult.Failure(
                WeatherInfoFetchException.FetchFailure(date, e)
            )
        } catch (e: InvalidParameterException) {
            // MEMO:このパスは通常 canFetchWeatherInfoUseCase で防がれる
            Log.e(logTag, "${logMsg}失敗_取得対象外の日付 (日付: $date)", e)
            UseCaseResult.Failure(
                WeatherInfoFetchException.DateOutOfRange(date, e)
            )
        }
    }
}
