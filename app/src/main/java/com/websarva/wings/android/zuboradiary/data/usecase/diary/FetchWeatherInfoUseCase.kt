package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.exception.LocationAccessFailedException
import com.websarva.wings.android.zuboradiary.data.exception.LocationPermissionException
import com.websarva.wings.android.zuboradiary.data.exception.WeatherInfoDateOutOfRangeException
import com.websarva.wings.android.zuboradiary.data.exception.WeatherInfoDateRangeCheckFailedException
import com.websarva.wings.android.zuboradiary.data.exception.WeatherInfoFetchFailedException
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class FetchWeatherInfoUseCase(
    private val weatherApiRepository: WeatherApiRepository,
    private val locationRepository: LocationRepository,
    private val canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(isGranted: Boolean, date: LocalDate): UseCaseResult<Weather> {
        if (!isGranted) return UseCaseResult.Error(LocationPermissionException())

        val geoCoordinates =
            try {
                fetchCurrentLocation()
            } catch (e: Exception) {
                return UseCaseResult.Error(e)
            }

        val weather =
            try {
                fetchWeatherInfo(date, geoCoordinates)
            } catch (e: Exception) {
                return UseCaseResult.Error(e)
            }
        return UseCaseResult.Success(weather)
    }

    private suspend fun fetchCurrentLocation(): GeoCoordinates {
        return try {
            locationRepository.fetchLocation() ?: throw LocationAccessFailedException()
        } catch (e: Exception) {
            throw LocationAccessFailedException(e)
        }
    }

    private suspend fun fetchWeatherInfo(date: LocalDate, geoCoordinates: GeoCoordinates): Weather {
        when (val result = canFetchWeatherInfoUseCase(date)) {
            is UseCaseResult.Success -> {
                if (!result.value) throw WeatherInfoDateOutOfRangeException()
            }
            is UseCaseResult.Error -> throw WeatherInfoDateRangeCheckFailedException(result.exception)
        }

        val logMsg = "天気情報取得"
        Log.i(logTag, "${logMsg}_開始")

        val currentDate = LocalDate.now()
        val betweenDays = ChronoUnit.DAYS.between(date, currentDate)

        val response =
            try {
                if (betweenDays == 0L) {
                    weatherApiRepository.fetchTodayWeatherInfo(geoCoordinates)
                } else {
                    weatherApiRepository.fetchPastDayWeatherInfo(
                        geoCoordinates,
                        betweenDays.toInt()
                    )
                }
            } catch (e: Exception) {
                throw WeatherInfoFetchFailedException(e)
            }
        Log.d(logTag, "fetchWeatherInformation()_code = " + response.code())
        Log.d(logTag, "fetchWeatherInformation()_message = :" + response.message())

        if (response.isSuccessful) {
            Log.d(logTag, "fetchWeatherInformation()_body = " + response.body())
            val result =
                response.body()?.toWeatherInfo() ?: throw IllegalStateException()
            Log.i(logTag, "${logMsg}_完了")
            return result
        } else {
            response.errorBody().use { errorBody ->
                val errorBodyString = errorBody?.string() ?: "null"
                Log.d(
                    logTag,
                    "fetchWeatherInformation()_errorBody = $errorBodyString"
                )
            }
            Log.e(logTag, "${logMsg}_失敗")
            throw WeatherInfoFetchFailedException()
        }
    }
}
