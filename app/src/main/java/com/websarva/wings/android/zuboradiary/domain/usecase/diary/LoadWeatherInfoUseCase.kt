package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.error.FetchWeatherInfoError
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.domain.exception.weather.AcquireWeatherInfoFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.location.ObtainCurrentLocationFailedException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class LoadWeatherInfoUseCase(
    private val weatherApiRepository: WeatherApiRepository,
    private val locationRepository: LocationRepository,
    private val canLoadWeatherInfoUseCase: CanLoadWeatherInfoUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(isGranted: Boolean, date: LocalDate): UseCaseResult<Weather, FetchWeatherInfoError> {
        val logMsg = "天気情報取得_"
        Log.i(logTag, "${logMsg}開始")

        val canLoadWeatherInfo = canLoadWeatherInfo(date)
        if (!canLoadWeatherInfo) {
            return UseCaseResult.Error(
                FetchWeatherInfoError.WeatherInfoDateOutOfRange()
            )
        }

        if (!isGranted) {
            val error = FetchWeatherInfoError.LocationPermissionNotGranted()
            Log.e(logTag, "${logMsg}位置情報権限未取得", error)
            return UseCaseResult.Error(error)
        }

        try {
            val geoCoordinates =fetchCurrentLocation()
            val weather = fetchWeatherInfo(date, geoCoordinates)
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(weather)
        } catch (e: FetchWeatherInfoError) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Error(e)
        }
    }

    private fun canLoadWeatherInfo(date: LocalDate): Boolean {
        return when (val result = canLoadWeatherInfoUseCase(date)) {
            is UseCaseResult.Success -> {
                result.value
            }
            is UseCaseResult.Error -> {
                // canLoadWeatherInfoUseCaseは常時Successの為、処理不要。
                false
            }
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
            throw FetchWeatherInfoError.AccessLocation(e)
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
            throw FetchWeatherInfoError.LoadWeatherInfo(e)
        }
    }
}
