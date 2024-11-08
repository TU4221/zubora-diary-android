package com.websarva.wings.android.zuboradiary.data.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.data.diary.Weather;

import java.util.Objects;
import java.util.concurrent.Callable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public abstract class WeatherApiCallable  implements Callable<Boolean> {

    private final Call<WeatherApiResponse> weatherApiResponseCall;

    public WeatherApiCallable(Call<WeatherApiResponse> weatherApiResponseCall) {
        Objects.requireNonNull(weatherApiResponseCall);

        this.weatherApiResponseCall = weatherApiResponseCall;
    }

    @Override
    public Boolean call() {
        try {
            Response<WeatherApiResponse> response = weatherApiResponseCall.execute();
            Log.d("WeatherApi", "response.code():" + response.code());
            Log.d("WeatherApi", "response.message():" + response.message());
            if (response.isSuccessful()) {
                WeatherApiResponse weatherApiResponse = response.body();
                Objects.requireNonNull(weatherApiResponse);
                Log.d("WeatherApi", "response.body():" + response.body());
                Weather weather = weatherApiResponse.toWeatherInfo();
                onResponse(weather);
                return true;
            } else {
                try(ResponseBody errorBody = response.errorBody()) {
                    Objects.requireNonNull(errorBody);
                    Log.d("WeatherApi", "response.errorBody():" + errorBody.string());
                }
                onFailure();
                return false;
            }
        } catch (Exception e) {
            Objects.requireNonNull(e);
            e.printStackTrace();
            onException(e);
            return false;
        }

    }

    public abstract void onResponse(@NonNull Weather weather);

    public abstract void onFailure();

    public abstract void onException(@NonNull Exception e);
}
