package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.exception.FetchWeatherInfoException
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult2
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

    suspend operator fun invoke(isGranted: Boolean, date: LocalDate): UseCaseResult2<Weather, FetchWeatherInfoException> {
        if (!isGranted) return UseCaseResult2.Error(FetchWeatherInfoException.LocationPermissionException())

        val geoCoordinates =
            try {
                fetchCurrentLocation()
            } catch (e: FetchWeatherInfoException) {
                return UseCaseResult2.Error(e)
            }

        val weather =
            try {
                fetchWeatherInfo(date, geoCoordinates)
            } catch (e: FetchWeatherInfoException) {
                return UseCaseResult2.Error(e)
            }
        return UseCaseResult2.Success(weather)
    }

    private suspend fun fetchCurrentLocation(): GeoCoordinates {
        val logMsg = "位置情報取得_"
        Log.i(logTag, "${logMsg}開始")
        return try {
            val result = locationRepository.fetchLocation()
            requireNotNull(result)
            Log.i(logTag, "${logMsg}完了")
            result
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗")
            throw FetchWeatherInfoException.LocationAccessFailedException(e)
        }
    }

    private suspend fun fetchWeatherInfo(date: LocalDate, geoCoordinates: GeoCoordinates): Weather {
        val logMsg = "天気情報取得_"
        Log.i(logTag, "${logMsg}開始")

        when (val result = canFetchWeatherInfoUseCase(date)) {
            is UseCaseResult.Success -> {
                if (!result.value) {
                    val e =
                        FetchWeatherInfoException
                            .WeatherInfoDateOutOfRangeException()
                    Log.i(logTag, "${logMsg}指定日範囲外", e)
                    throw e
                }
            }
            is UseCaseResult.Error -> {
                val e =
                    FetchWeatherInfoException
                        .WeatherInfoDateRangeCheckFailedException(result.exception)
                Log.i(logTag, "${logMsg}取得可能確認失敗", e)
                throw e
            }
        }

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
                Log.e(logTag, "${logMsg}失敗", e)
                throw FetchWeatherInfoException.WeatherInfoFetchFailedException(e)
            }
        Log.d(logTag, "fetchWeatherInformation()_code = " + response.code())
        Log.d(logTag, "fetchWeatherInformation()_message = :" + response.message())

        return if (response.isSuccessful) {
            Log.d(logTag, "fetchWeatherInformation()_body = " + response.body())
            val result =
                response.body()?.toWeatherInfo() ?: throw NullPointerException()
            Log.i(logTag, "${logMsg}完了")
            result
        } else {
            response.errorBody().use { errorBody ->
                val errorBodyString = errorBody?.string() ?: "null"
                Log.d(
                    logTag,
                    "fetchWeatherInformation()_errorBody = $errorBodyString"
                )
            }
            val e = FetchWeatherInfoException.WeatherInfoFetchFailedException()
            Log.e(logTag, "${logMsg}失敗", e)
            throw e
        }
    }
}
