package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource

internal class UriRepository (
    private val dataSource: UriPermissionDataSource
) {
    fun takePersistablePermission(uri: Uri) {
        dataSource.takePersistablePermission(uri)
    }

    fun releasePersistablePermission(uri: Uri) {
        dataSource.releasePersistablePermission(uri)
    }

    fun releaseAllPersistablePermission() {
        dataSource.releaseAllPersistablePermission()
    }
}
