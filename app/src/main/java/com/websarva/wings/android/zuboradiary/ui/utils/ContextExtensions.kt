package com.websarva.wings.android.zuboradiary.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * 位置情報へのアクセス権限
 * （[Manifest.permission.ACCESS_FINE_LOCATION]、又は[Manifest.permission.ACCESS_COARSE_LOCATION]）
 * が付与されているかを確認する。
 */
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

/**
 * [Build.VERSION_CODES.TIRAMISU]以降で、
 * 通知の投稿権限（[Manifest.permission.POST_NOTIFICATIONS]）が付与されているかを確認する。
 */
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
