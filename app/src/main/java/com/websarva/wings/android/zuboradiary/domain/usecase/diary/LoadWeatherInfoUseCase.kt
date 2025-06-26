package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.AcquireWeatherInfoUseCaseException
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.domain.exception.weather.AcquireWeatherInfoFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.location.ObtainCurrentLocationFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class LoadWeatherInfoUseCase(
    private val weatherApiRepository: WeatherApiRepository,
    private val locationRepository: LocationRepository,
    private val canLoadWeatherInfoUseCase: CanLoadWeatherInfoUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(isGranted: Boolean, date: LocalDate): DefaultUseCaseResult<Weather> {
        val logMsg = "天気情報取得_"
        Log.i(logTag, "${logMsg}開始")

        if (!canLoadWeatherInfoUseCase(date).value) {
            return UseCaseResult.Failure(
                AcquireWeatherInfoUseCaseException.WeatherInfoDateOutOfRange()
            )
        }

        if (!isGranted) {
            val error = AcquireWeatherInfoUseCaseException.LocationPermissionNotGranted()
            Log.e(logTag, "${logMsg}位置情報権限未取得", error)
            return UseCaseResult.Failure(error)
        }

        try {
            val geoCoordinates =fetchCurrentLocation()
            val weather = fetchWeatherInfo(date, geoCoordinates)
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(weather)
        } catch (e: AcquireWeatherInfoUseCaseException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }

    private suspend fun fetchCurrentLocation(): GeoCoordinates {
        val logMsg = "位置情報取得_"
        Log.i(logTag, "${logMsg}開始")
        return try {
            val result = locationRepository.fetchLocation()
            Log.i(logTag, "${logMsg}完了")
            result
        } catch (e: ObtainCurrentLocationFailedException) {
            throw AcquireWeatherInfoUseCaseException.AccessLocationFailed(e)
        }
    }

    private suspend fun fetchWeatherInfo(date: LocalDate, geoCoordinates: GeoCoordinates): Weather {
        val logMsg = "天気情報Api通信_"
        Log.i(logTag, "${logMsg}開始")

        val currentDate = LocalDate.now()
        val betweenDays = ChronoUnit.DAYS.between(date, currentDate)

        return try {
            val result = if (betweenDays == 0L) {
                weatherApiRepository.fetchTodayWeatherInfo(geoCoordinates)
            } else {
                weatherApiRepository.fetchPastDayWeatherInfo(
                    geoCoordinates,
                    betweenDays.toInt()
                )
            }
            Log.i(logTag, "${logMsg}完了")
            result
        } catch (e: AcquireWeatherInfoFailedException) {
            Log.i(logTag, "${logMsg}失敗")
            throw AcquireWeatherInfoUseCaseException.AcquireWeatherInfoFailed(e)
        }
    }
}
