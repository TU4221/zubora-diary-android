package com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.exception.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.repository.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NetworkConnectionException
import com.websarva.wings.android.zuboradiary.domain.usecase.location.FetchCurrentLocationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.location.exception.CurrentLocationFetchException
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
    private val canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase,
    private val fetchCurrentLocationUseCase: FetchCurrentLocationUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "天気情報取得_"

    /**
     * ユースケースを実行し、指定された日付の天気情報を取得する。
     *
     * @param isGranted 位置情報権限が付与されているかどうか。
     * @param date 天気情報を取得する日付。
     * @return 処理に成功した場合は [UseCaseResult.Success] に [Weather] を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [WeatherInfoFetchException] を格納して返す。
     */
    suspend operator fun invoke(
        isGranted: Boolean,
        date: LocalDate
    ): UseCaseResult<Weather, WeatherInfoFetchException> {
        Log.i(logTag, "${logMsg}開始 (権限付与: $isGranted, 日付: $date)")

        if (!canFetchWeatherInfoUseCase(date).value) {
            val exception = WeatherInfoFetchException.DateOutOfRange(date)
            Log.w(logTag, "${logMsg}失敗_取得対象外の日付 (日付: $date)", exception)
            return UseCaseResult.Failure(exception)
        }

        return try {
            val location =
                when (val result = fetchCurrentLocationUseCase(isGranted)) {
                    is UseCaseResult.Success -> result.value
                    is UseCaseResult.Failure -> throw result.exception
                }
            val weather = weatherInfoRepository.fetchWeatherInfo(date, location)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(weather)
        } catch (e: CurrentLocationFetchException) {
            when (e) {
                is CurrentLocationFetchException.LocationPermissionNotGranted -> {
                    Log.w(logTag, "${logMsg}失敗_位置情報権限未取得", e)
                    UseCaseResult.Failure(
                        WeatherInfoFetchException.LocationPermissionNotGranted(e)
                    )
                }
                is CurrentLocationFetchException.LocationAccessFailure -> {
                    Log.e(logTag, "${logMsg}失敗_位置情報アクセスエラー", e)
                    UseCaseResult.Failure(
                        WeatherInfoFetchException.LocationAccessFailure(e)
                    )
                }
            }
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
