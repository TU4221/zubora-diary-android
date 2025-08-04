package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.data.uri.PersistableUriPermissionOperationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionTakeFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.AllPersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionReleaseFailureException

internal class UriRepository (
    private val dataSource: UriPermissionDataSource
) {

    @Throws(PersistableUriPermissionTakeFailureException::class)
    fun takePersistableUriPermission(uriString: String) {
        val uri = Uri.parse(uriString)
        try {
            dataSource.takePersistableUriPermission(uri)
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw PersistableUriPermissionTakeFailureException(uri, e)
        }
    }

    @Throws(PersistableUriPermissionReleaseFailureException::class)
    fun releasePersistableUriPermission(uriString: String) {
        val uri = Uri.parse(uriString)
        try {
            dataSource.releasePersistableUriPermission(uri)
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw PersistableUriPermissionReleaseFailureException(uri, e)
        }
    }

    @Throws(AllPersistableUriPermissionReleaseFailureException::class)
    fun releaseAllPersistableUriPermission() {
        try {
            dataSource.releaseAllPersistableUriPermission()
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw AllPersistableUriPermissionReleaseFailureException(e)
        }
    }
}
