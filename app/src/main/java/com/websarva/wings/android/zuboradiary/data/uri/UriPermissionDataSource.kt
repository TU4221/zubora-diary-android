package com.websarva.wings.android.zuboradiary.data.uri

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class UriPermissionDataSource (
    private val resolver: ContentResolver
) {

    private val logTag = createLogTag()

    fun takePersistablePermission(uri: Uri) {
        val logMsg = "端末写真使用権限取得"
        Log.i(logTag, "${logMsg}_開始=$uri")

        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            Log.e(logTag, "${logMsg}_失敗", e)
            // 対処できないがアプリを落としたくない為、catchのみの処理とする。
            return
        }
        Log.i(logTag, "${logMsg}_完了")
    }

    // MEMO:Uri先のファイルを削除すると、登録されていたUriPermissionも同時に削除される。
    fun releasePersistablePermission(uri: Uri) {
        val logMsg = "端末写真使用権限解放"
        Log.i(logTag, "${logMsg}_開始_URI=$uri")

        val permissionList = resolver.persistedUriPermissions
        for (uriPermission in permissionList) {
            val permittedUri = uriPermission.uri
            val permittedUriString = permittedUri.toString()
            val targetUriString = uri.toString()

            if (permittedUriString == targetUriString) {
                try {
                    resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: SecurityException) {
                    Log.e(logTag, "${logMsg}_失敗", e)
                    // 対処できないがアプリを落としたくない為、catchのみの処理とする。
                    return
                }
                break
            }
        }

        Log.i(logTag, "${logMsg}_完了")
    }

    fun releaseAllPersistablePermission() {
        val logMsg = "端末写真使用権限全解放"
        Log.i(logTag, "${logMsg}_開始")

        val permissionList = resolver.persistedUriPermissions
        for (uriPermission in permissionList) {
            val uri = uriPermission.uri
            try {
                resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                Log.e(logTag, "${logMsg}_失敗", e)
                // 対処できないがアプリを落としたくない為、catchのみの処理とする。
                return
            }
        }

        Log.i(logTag, "${logMsg}_完了")
    }
}
