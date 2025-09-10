package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.location.FetchCurrentLocationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.CanFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.FetchWeatherInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 天気情報取得関連のユースケースの依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object WeatherInfoUseCaseModule {

    @Singleton
    @Provides
    fun provideCanFetchWeatherInfoUseCase(
        weatherInfoRepository: WeatherInfoRepository
    ): CanFetchWeatherInfoUseCase = CanFetchWeatherInfoUseCase(weatherInfoRepository)

    @Singleton
    @Provides
    fun provideFetchWeatherInfoUseCase(
        weatherInfoRepository: WeatherInfoRepository,
        canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase,
        fetchCurrentLocationUseCase: FetchCurrentLocationUseCase
    ): FetchWeatherInfoUseCase =
        FetchWeatherInfoUseCase(
            weatherInfoRepository,
            canFetchWeatherInfoUseCase,
            fetchCurrentLocationUseCase
        )
}
