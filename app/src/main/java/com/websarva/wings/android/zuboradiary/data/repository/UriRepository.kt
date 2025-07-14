package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.data.uri.PersistableUriPermissionOperationException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.TakePersistableUriPermissionFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.ReleaseAllPersistableUriPermissionFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.ReleasePersistableUriPermissionFailedException

internal class UriRepository (
    private val dataSource: UriPermissionDataSource
) {

    @Throws(TakePersistableUriPermissionFailedException::class)
    fun takePersistableUriPermission(uriString: String) {
        val uri = Uri.parse(uriString)
        try {
            dataSource.takePersistableUriPermission(uri)
        } catch (e: PersistableUriPermissionOperationException) {
            throw TakePersistableUriPermissionFailedException(uri, e)
        }
    }

    @Throws(ReleasePersistableUriPermissionFailedException::class)
    fun releasePersistableUriPermission(uriString: String) {
        val uri = Uri.parse(uriString)
        try {
            dataSource.releasePersistableUriPermission(uri)
        } catch (e: PersistableUriPermissionOperationException) {
            throw ReleasePersistableUriPermissionFailedException(uri, e)
        }
    }

    @Throws(ReleaseAllPersistableUriPermissionFailedException::class)
    fun releaseAllPersistableUriPermission() {
        try {
            dataSource.releaseAllPersistableUriPermission()
        } catch (e: PersistableUriPermissionOperationException) {
            throw ReleaseAllPersistableUriPermissionFailedException(e)
        }
    }
}
