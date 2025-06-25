package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionOperationException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.error.UriError

internal class UriRepository (
    private val dataSource: UriPermissionDataSource
) {

    @Throws(UriError.TakePermission::class)
    fun takePersistablePermission(uri: Uri) {
        try {
            dataSource.takePersistablePermission(uri)
        } catch (e: UriPermissionOperationException) {
            throw UriError.TakePermission(e)
        }
    }

    @Throws(UriError.ReleasePermission::class)
    fun releasePersistablePermission(uri: Uri) {
        try {
            dataSource.releasePersistablePermission(uri)
        } catch (e: UriPermissionOperationException) {
            throw UriError.ReleasePermission(e)
        }
    }

    @Throws(UriError.ReleasePermission::class)
    fun releaseAllPersistablePermission() {
        try {
            dataSource.releaseAllPersistablePermission()
        } catch (e: UriPermissionOperationException) {
            throw UriError.ReleasePermission(e)
        }
    }
}
