package com.websarva.wings.android.zuboradiary.ui.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal abstract class UriPermissionManager {

    private val logTag = createLogTag()

    fun takePersistablePermission(context: Context, uri: Uri) {
        val logMsg = "端末写真使用権限取得"
        Log.d(logTag, "${logMsg}_開始=$uri")

        val resolver = context.contentResolver
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            // 対処できないがアプリを落としたくない為、catchのみの処理とする。
        }
        Log.d(logTag, "${logMsg}_完了")
    }

    /**
     * 対象Uriが他で使用されていないかを確認するコードを記述すること。権限解放時、このメソッドが処理される。
     */
    abstract suspend fun checkUsedUriDoesNotExist(uri: Uri): Boolean?

    // MEMO:Uri先のファイルを削除すると、登録されていたUriPermissionも同時に削除される。
    suspend fun releasePersistablePermission(context: Context, uri: Uri) {
        val logMsg = "端末写真使用権限解放"
        Log.d(logTag, "${logMsg}_開始_URI=$uri")

        val resolver = context.contentResolver
        val permissionList = resolver.persistedUriPermissions
        for (uriPermission in permissionList) {
            val permittedUri = uriPermission.uri
            val permittedUriString = permittedUri.toString()
            val targetUriString = uri.toString()

            if (permittedUriString == targetUriString) {
                if (!(checkUsedUriDoesNotExist(uri) ?: return)) return

                resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                return
            }
        }

        Log.d(logTag, "${logMsg}_完了")
    }

    fun releaseAllPersistablePermission(context: Context) {
        val logMsg = "端末写真使用権限全解放"
        Log.d(logTag, "${logMsg}_開始")

        val resolver = context.contentResolver
        val permissionList = resolver.persistedUriPermissions
        for (uriPermission in permissionList) {
            val uri = uriPermission.uri
            resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Log.d(logTag, "${logMsg}_完了")
    }
}
