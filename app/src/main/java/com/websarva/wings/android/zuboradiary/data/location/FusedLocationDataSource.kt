package com.websarva.wings.android.zuboradiary.data.location

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.websarva.wings.android.zuboradiary.data.location.exception.InvalidLocationRequestParameterException
import com.websarva.wings.android.zuboradiary.data.location.exception.LocationAccessException
import com.websarva.wings.android.zuboradiary.data.location.exception.LocationProviderException
import com.websarva.wings.android.zuboradiary.data.location.exception.LocationUnavailableException
import com.websarva.wings.android.zuboradiary.data.location.exception.PermissionDeniedException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Fused Location Provider APIを利用して位置情報を取得するデータソースクラス。
 *
 * このクラスは、デバイスの現在位置を取得する機能を提供する。
 * 位置情報に関するエラーは、定義されたカスタム例外 ([LocationProviderException] のサブクラスなど) をスローする。
 *
 * @property fusedLocationProviderClient デバイスの位置情報を取得するために使用される。
 * @property dispatcher 位置情報の取得を実行するスレッドプール。
 */
internal class FusedLocationDataSource(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // MEMO:fusedLocationProviderClient.lastLocation()を記述する時、Permission確認コードが必須となるが、
    //      Permission確認はプロパティで管理する為、@SuppressLintで警告抑制。
    /**
     * デバイスの現在位置を取得する。
     *
     * 指定されたタイムアウト時間内に位置情報を取得できなかった場合、または位置情報へのアクセス権限がない場合は例外をスローする。
     *
     * @param timeoutMillis タイムアウトまでの時間 (ミリ秒単位)。デフォルトは10000ミリ秒。(1ミリ秒以上であること)
     * @return 取得した位置情報。
     * @throws PermissionDeniedException 位置情報へのアクセス権限がない場合。
     * @throws LocationAccessException 位置情報のアクセスに失敗した場合。
     * @throws LocationUnavailableException 指定時間内に位置情報を取得できない、または現在地が特定できない場合。
     * @throws InvalidLocationRequestParameterException 引数が不正な場合。
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(
        timeoutMillis: Long = 10000L
    ): Location {
        try {
            require(timeoutMillis >= 1) { "タイムアウトまでの時間 `$timeoutMillis` が不正値" }
        } catch (e: IllegalArgumentException) {
            throw InvalidLocationRequestParameterException(e)
        }

        return withContext(dispatcher) {
            val logMsg = "現在位置取得"
            Log.i(logTag, "${logMsg}_開始")
            val cancellationTokenSource = CancellationTokenSource()
            try {
                return@withContext withTimeout(timeoutMillis) {
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
                        Log.w(logTag, "${logMsg}_失敗_位置情報:null")
                        throw LocationUnavailableException()
                    }

                    Log.i(logTag, "${logMsg}_完了_位置情報:$location")
                    return@withTimeout location
                }
            } catch (e: SecurityException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                throw PermissionDeniedException(e)
            } catch (e: IllegalStateException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                throw LocationAccessException(e)
            } catch (e: TimeoutCancellationException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                throw LocationUnavailableException(e)
            } finally {
                cancellationTokenSource.cancel()
            }
        }
    }
}
