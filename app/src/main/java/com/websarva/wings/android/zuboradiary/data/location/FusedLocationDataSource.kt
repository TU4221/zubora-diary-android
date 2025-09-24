package com.websarva.wings.android.zuboradiary.data.location

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.websarva.wings.android.zuboradiary.data.common.PermissionChecker
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeoutException

/**
 * Fused Location Provider APIを利用して位置情報を取得するデータソースクラス。
 *
 * このクラスは、デバイスの現在位置を取得する機能を提供する。
 * 位置情報へのアクセス権限がない場合や、タイムアウトが発生した場合に発生する特定の例外を
 * [FusedLocationAccessFailureException] にラップする。
 *
 * @param fusedLocationProviderClient デバイスの位置情報を取得するために使用される。
 * @param permissionChecker 位置情報アクセスパーミッションなど、必要な権限が付与されているかを確認する機能を提供。
 * @property dispatcher 位置情報の取得を実行するスレッドプール。
 */
internal class FusedLocationDataSource(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val permissionChecker: PermissionChecker,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val logTag = createLogTag()

    // MEMO:fusedLocationProviderClient.lastLocation()を記述する時、Permission確認コードが必須となるが、
    //      Permission確認はプロパティで管理する為、@SuppressLintで警告抑制。
    /**
     * デバイスの現在位置を取得する。
     *
     * 指定されたタイムアウト時間内に位置情報を取得できなかった場合、または位置情報へのアクセス権限がない場合は例外をスローする。
     *
     * @param timeoutMillis タイムアウトまでの時間 (ミリ秒単位)。デフォルトは10000ミリ秒。
     * @return 取得した位置情報。
     * @throws FusedLocationAccessFailureException 位置情報の取得に失敗した場合 (権限不足、タイムアウト、その他の内部エラー)。
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(timeoutMillis: Long = 10000L): Location {
        return withContext(dispatcher) {
            val logMsg = "現在位置取得"
            Log.i(logTag, "${logMsg}_開始")
            val cancellationTokenSource = CancellationTokenSource()
            try {
                if (!permissionChecker.isAccessLocationGranted) {
                    Log.i(logTag, "${logMsg}_権限未許可")
                    throw SecurityException()
                }
                return@withContext withTimeoutOrNull(timeoutMillis) {
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
                    return@withTimeoutOrNull location
                } ?: run {
                    Log.w(logTag, "${logMsg}_失敗_location:null")
                    throw TimeoutException()
                }
            } catch (e: SecurityException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                throw FusedLocationAccessFailureException(e)
            } catch (e: IllegalStateException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                throw FusedLocationAccessFailureException(e)
            } catch (e: TimeoutException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                throw FusedLocationAccessFailureException(e)
            } finally {
                cancellationTokenSource.cancel()
            }
        }
    }
}
