package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import retrofit2.Call
import java.util.concurrent.Callable

abstract class WeatherApiCallable(private val weatherApiResponseCall: Call<WeatherApiResponse>)
    : Callable<Boolean> {

    override fun call(): Boolean {
        try {
            val response = weatherApiResponseCall.execute()
            Log.d("WeatherApi", "response.code():" + response.code())
            Log.d("WeatherApi", "response.message():" + response.message())
            if (response.isSuccessful) {
                val weatherApiResponse = response.body()
                Log.d("WeatherApi", "response.body():" + response.body())
                val weather = weatherApiResponse!!.toWeatherInfo()
                onResponse(weather)
                return true
            } else {
                response.errorBody().use { errorBody ->
                    Log.d("WeatherApi", "response.errorBody():" + errorBody!!.string())
                }
                onFailure()
                return false
            }
        } catch (e: Exception) {
            Log.d("Exception", "WeatherApi読込失敗", e)
            onException(e)
            return false
        }
    }

    abstract fun onResponse(weather: Weather)

    abstract fun onFailure()

    abstract fun onException(e: Exception)
}
