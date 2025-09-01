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

internal class FetchWeatherInfoUseCase(
    private val weatherInfoRepository: WeatherInfoRepository,
    private val canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(isGranted: Boolean, date: LocalDate): DefaultUseCaseResult<Weather> {
        val logMsg = "天気情報取得_"
        Log.i(logTag, "${logMsg}開始")

        if (!canFetchWeatherInfoUseCase(date).value) {
            return UseCaseResult.Failure(
                FetchWeatherInfoUseCaseException.WeatherInfoDateOutOfRange(date)
            )
        }

        if (!isGranted) {
            val error = FetchWeatherInfoUseCaseException.LocationPermissionNotGranted()
            Log.e(logTag, "${logMsg}位置情報権限未取得", error)
            return UseCaseResult.Failure(error)
        }

        try {
            val weather = fetchWeatherInfo(date)
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(weather)
        } catch (e: FetchWeatherInfoUseCaseException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }
    }

    private suspend fun fetchWeatherInfo(date: LocalDate): Weather {
        val logMsg = "天気情報取得_"
        Log.i(logTag, "${logMsg}開始")

        return try {
            val result = weatherInfoRepository.fetchWeatherInfo(date)
            Log.i(logTag, "${logMsg}完了")
            result
        } catch (e: WeatherInfoFetchException) {
            Log.e(logTag, "${logMsg}失敗")
            when (e) {
                is WeatherInfoFetchException.AccessLocationFailure ->
                    throw FetchWeatherInfoUseCaseException.LocationAccessFailure(e)
                is WeatherInfoFetchException.ApiAccessFailure ->
                    throw FetchWeatherInfoUseCaseException.WeatherInfoFetchFailure(e)
                is WeatherInfoFetchException.DateOutOfRange ->
                    throw FetchWeatherInfoUseCaseException.WeatherInfoDateOutOfRange(date, e)
            }
        }
    }
}
