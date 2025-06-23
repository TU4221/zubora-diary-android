package com.websarva.wings.android.zuboradiary.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.ui.utils.isAccessLocationGranted
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

internal class FusedLocationDataSource(
    private val context: Context
) {

    val logTag = createLogTag()

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // MEMO:fusedLocationProviderClient.lastLocation()を記述する時、Permission確認コードが必須となるが、
    //      Permission確認はプロパティで管理する為、@SuppressLintで警告抑制。
    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(timeoutMillis: Long = 10000L): GeoCoordinates? {
        val logMsg = "現在位置取得"
        Log.i(logTag, "${logMsg}_開始")
        val cancellationTokenSource = CancellationTokenSource()
        try {
            if (!context.isAccessLocationGranted()) {
                Log.i(logTag, "${logMsg}_権限未許可")
                return null
            }
            return withTimeoutOrNull(timeoutMillis) {
                val locationRequest =
                    CurrentLocationRequest.Builder()
                        // MEMO:"PRIORITY_BALANCED_POWER_ACCURACY"だとnullが返ってくることがある為、
                        //      "PRIORITY_HIGH_ACCURACY"とする。
                        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setDurationMillis(timeoutMillis)
                        .build()
                val location =
                    fusedLocationProviderClient.getCurrentLocation(
                        locationRequest,
                        cancellationTokenSource.token
                    ).await()

                if (location == null) {
                    return@withTimeoutOrNull null
                } else {
                    Log.i(logTag, "${logMsg}_完了_location:$location")
                }
                return@withTimeoutOrNull GeoCoordinates(location.latitude, location.longitude)
            } ?: run {
                Log.w(logTag, "${logMsg}_失敗_location:null")
                return@run null
            }
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            throw e
        } finally {
            cancellationTokenSource.cancel()
        }
    }
}
