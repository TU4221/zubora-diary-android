package com.websarva.wings.android.zuboradiary.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

internal fun Context.isGrantedAccessLocation(): Boolean {
    val isGrantedAccessFineLocation =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    Log.d(
        this.javaClass.name,
        "Context.isGrantedAccessLocation()_isGrantedAccessFineLocation = $isGrantedAccessFineLocation"
    )

    val isGrantedAccessCoarseLocation =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    Log.d(
        this.javaClass.name,
        "Context.isGrantedAccessLocation()_isGrantedAccessCoarseLocation = $isGrantedAccessCoarseLocation"
    )

    return isGrantedAccessFineLocation || isGrantedAccessCoarseLocation
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun Context.isGrantedPostNotifications(): Boolean {
    val isGranted =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )
                == PackageManager.PERMISSION_GRANTED)
    Log.d(
        this.javaClass.name,
        "Context.isGrantedPostNotifications() = $isGranted"
    )
    return isGranted
}
