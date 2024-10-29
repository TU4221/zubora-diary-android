package com.websarva.wings.android.zuboradiary.data.network;

import android.util.Log;

import com.websarva.wings.android.zuboradiary.data.AppError;
import com.websarva.wings.android.zuboradiary.data.diary.Weathers;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public abstract class WeatherApiCallable  implements Callable<Boolean> {

    private final Call<WeatherApiResponse> weatherApiResponseCall;

    public WeatherApiCallable(Call<WeatherApiResponse> weatherApiResponseCall) {
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
                Weathers weather = weatherApiResponse.toWeatherInformation();
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
            e.printStackTrace();
            onException(e);
            return false;
        }

    }

    public abstract void onResponse(Weathers weather);

    public abstract void onFailure();

    public abstract void onException(Exception e);
}
