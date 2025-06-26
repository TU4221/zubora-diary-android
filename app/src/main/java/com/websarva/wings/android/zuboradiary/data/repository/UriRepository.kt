package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionOperationException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.EnsurePersistentAccessUriFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.RevokePersistentAccessAllUriFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.RevokePersistentAccessUriFailedException

internal class UriRepository (
    private val dataSource: UriPermissionDataSource
) {

    @Throws(EnsurePersistentAccessUriFailedException::class)
    fun takePersistablePermission(uri: Uri) {
        try {
            dataSource.takePersistablePermission(uri)
        } catch (e: UriPermissionOperationException) {
            throw EnsurePersistentAccessUriFailedException(uri, e)
        }
    }

    @Throws(RevokePersistentAccessUriFailedException::class)
    fun releasePersistablePermission(uri: Uri) {
        try {
            dataSource.releasePersistablePermission(uri)
        } catch (e: UriPermissionOperationException) {
            throw RevokePersistentAccessUriFailedException(uri, e)
        }
    }

    @Throws(RevokePersistentAccessAllUriFailedException::class)
    fun releaseAllPersistablePermission() {
        try {
            dataSource.releaseAllPersistablePermission()
        } catch (e: UriPermissionOperationException) {
            throw RevokePersistentAccessAllUriFailedException(e)
        }
    }
}
