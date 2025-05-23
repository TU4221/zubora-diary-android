package com.websarva.wings.android.zuboradiary.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

internal fun Context.isAccessLocationGranted(): Boolean {
    val isAccessFineLocationGranted =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    Log.d(
        this.javaClass.name,
        "Context.isAccessLocationGranted()_isAccessFineLocationGranted = $isAccessFineLocationGranted"
    )

    val isAccessCoarseLocationGranted =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
    Log.d(
        this.javaClass.name,
        "Context.isAccessLocationGranted()_isAccessCoarseLocationGranted = $isAccessCoarseLocationGranted"
    )

    return isAccessFineLocationGranted || isAccessCoarseLocationGranted
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun Context.isPostNotificationsGranted(): Boolean {
    val isGranted =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )
                == PackageManager.PERMISSION_GRANTED)
    Log.d(
        this.javaClass.name,
        "Context.isPostNotificationsGranted() = $isGranted"
    )
    return isGranted
}
