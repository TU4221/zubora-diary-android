package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.data.uri.PersistableUriPermissionOperationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionTakeFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.AllPersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.repository.UriRepository

internal class UriRepositoryImpl (
    private val dataSource: UriPermissionDataSource
) : UriRepository {

    @Throws(PersistableUriPermissionTakeFailureException::class)
    override fun takePersistableUriPermission(uriString: String) {
        val uri = Uri.parse(uriString)
        try {
            dataSource.takePersistableUriPermission(uri)
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw PersistableUriPermissionTakeFailureException(uri, e)
        }
    }

    @Throws(PersistableUriPermissionReleaseFailureException::class)
    override fun releasePersistableUriPermission(uriString: String) {
        val uri = Uri.parse(uriString)
        try {
            dataSource.releasePersistableUriPermission(uri)
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw PersistableUriPermissionReleaseFailureException(uri, e)
        }
    }

    @Throws(AllPersistableUriPermissionReleaseFailureException::class)
    override fun releaseAllPersistableUriPermission() {
        try {
            dataSource.releaseAllPersistableUriPermission()
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw AllPersistableUriPermissionReleaseFailureException(e)
        }
    }
}
