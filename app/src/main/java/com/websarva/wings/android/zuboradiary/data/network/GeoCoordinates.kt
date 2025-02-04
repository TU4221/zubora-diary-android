package com.websarva.wings.android.zuboradiary.data.network;

import androidx.annotation.FloatRange;

public class GeoCoordinates {

    @FloatRange(from = -90.0, to = 90.0)
    private final double latitude;
    @FloatRange(from = -180.0, to = 180.0)
    private final double longitude;

    public GeoCoordinates(double latitude, double longitude) {
        if (latitude < -90) throw new IllegalArgumentException();
        if (latitude > 90) throw new IllegalArgumentException();
        if (longitude < -180) throw new IllegalArgumentException();
        if (longitude > 180) throw new IllegalArgumentException();

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
