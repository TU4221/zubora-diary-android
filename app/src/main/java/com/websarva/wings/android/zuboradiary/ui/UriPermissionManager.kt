package com.websarva.wings.android.zuboradiary.ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri

abstract class UriPermissionManager(context: Context) {

    private val resolver: ContentResolver = context.contentResolver

    fun takePersistablePermission(uri: Uri) {
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // 対処できないがアプリを落としたくない為、catchのみの処理とする。
        }
    }

    /**
     * 対象Uriが他で使用されていないかを確認するコードを記述すること。権限解放時、このメソッドが処理される。
     */
    abstract fun checkUsedUriDoesNotExist(uri: Uri): Boolean

    // MEMO:Uri先のファイルを削除すると、登録されていたUriPermissionも同時に削除される。
    fun releasePersistablePermission(uri: Uri) {
        val permissionList = resolver.persistedUriPermissions
        for (uriPermission in permissionList) {
            val permittedUri = uriPermission.uri
            val permittedUriString = permittedUri.toString()
            val targetUriString = uri.toString()

            if (permittedUriString == targetUriString) {
                if (!checkUsedUriDoesNotExist(uri)) return

                resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                return
            }
        }
    }

    fun releaseAllPersistablePermission() {
        val permissionList = resolver.persistedUriPermissions
        for (uriPermission in permissionList) {
            val uri = uriPermission.uri
            resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
