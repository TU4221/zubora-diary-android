package com.websarva.wings.android.zuboradiary.data.model

import androidx.annotation.FloatRange

class GeoCoordinates(latitude: Double, longitude: Double) {

    @FloatRange(from = -90.0, to = 90.0)
    val latitude: Double
    @FloatRange(from = -180.0, to = 180.0)
    val longitude: Double

    init {
        require(latitude >= -90)
        require(latitude <= 90)
        require(longitude >= -180)
        require(longitude <= 180)

        this.latitude = latitude
        this.longitude = longitude
    }
}
