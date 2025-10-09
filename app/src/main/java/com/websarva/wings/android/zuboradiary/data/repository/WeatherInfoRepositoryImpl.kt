package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.weather.WeatherInfoRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.weather.toDomainModel
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource
import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkOperationException
import com.websarva.wings.android.zuboradiary.domain.model.location.SimpleLocation
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.exception.InvalidParameterException
import java.time.LocalDate

internal class WeatherInfoRepositoryImpl (
    private val weatherApiDataSource: WeatherApiDataSource,
    private val weatherInfoRepositoryExceptionMapper: WeatherInfoRepositoryExceptionMapper
) : WeatherInfoRepository {

    override fun canFetchWeatherInfo(date: LocalDate): Boolean {
        return weatherApiDataSource.canFetchWeatherInfo(date)
    }

    override suspend fun fetchWeatherInfo(date: LocalDate, location: SimpleLocation): Weather {
        return try {
            weatherApiDataSource
                .fetchWeatherInfo(date, location.latitude, location.longitude)
                .toDomainModel()
        } catch (e: IllegalArgumentException) {
            // 指定された日付が許容範囲外の場合(位置情報はSimpleLocationで正常値を保障されている)
            throw InvalidParameterException(cause = e)
        } catch (e: NetworkOperationException) {
            throw weatherInfoRepositoryExceptionMapper.toDomainException(e)
        }
    }
}
