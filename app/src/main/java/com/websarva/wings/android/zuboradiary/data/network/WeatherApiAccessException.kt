package com.websarva.wings.android.zuboradiary.data.network

import com.websarva.wings.android.zuboradiary.data.model.DataException

internal class WeatherApiAccessException (
    cause: Throwable? = null
) : DataException("天気情報の取得に失敗しました。", cause)
