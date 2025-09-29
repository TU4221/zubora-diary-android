package com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.exception.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.domain.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.exception.LocationException
import com.websarva.wings.android.zuboradiary.domain.exception.NetworkConnectionException
import com.websarva.wings.android.zuboradiary.domain.exception.PermissionException
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

        return try {
            if (!weatherInfoRepository.canFetchWeatherInfo(date)) {
                throw InvalidParameterException()
            }
            val location = locationRepository.fetchCurrentLocation()
            val weather = weatherInfoRepository.fetchWeatherInfo(date, location)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(weather)
        } catch (e: InvalidParameterException) {
            Log.e(logTag, "${logMsg}失敗_取得対象外の日付 (日付: $date)", e)
            UseCaseResult.Failure(
                WeatherInfoFetchException.DateOutOfRange(date, e)
            )
        } catch (e: PermissionException) {
            Log.e(logTag, "${logMsg}失敗_位置情報権限未取得", e)
            UseCaseResult.Failure(
                WeatherInfoFetchException.LocationPermissionNotGranted(e) // TODO:権限確認はUI側で行うようにし、データ層からの権限例外はユースケース失敗例外にラップする
            )
        } catch (e: LocationException) {
            Log.e(logTag, "${logMsg}失敗_位置情報アクセスエラー", e)
            UseCaseResult.Failure(
                WeatherInfoFetchException.LocationAccessFailure(e)
            )
        } catch (e: NetworkConnectionException) {
            Log.e(logTag, "${logMsg}失敗_APIアクセスエラー", e)
            UseCaseResult.Failure(
                WeatherInfoFetchException.FetchFailure(date, e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                WeatherInfoFetchException.Unknown(e)
            )
        }
    }
}
