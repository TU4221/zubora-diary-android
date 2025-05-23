package com.websarva.wings.android.zuboradiary.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Context.isGrantedAccessLocation(): Boolean {
    val isGrantedAccessFineLocation =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    Log.d(
        this.javaClass.name,
        "isGrantedAccessLocation.get()_FineLocation = $isGrantedAccessFineLocation"
    )

    val isGrantedAccessCoarseLocation =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    Log.d(
        this.javaClass.name,
        "isGrantedAccessLocation.get()_CoarseLocation = $isGrantedAccessCoarseLocation"
    )

    return isGrantedAccessFineLocation || isGrantedAccessCoarseLocation
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Context.isGrantedPostNotifications(): Boolean {
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
