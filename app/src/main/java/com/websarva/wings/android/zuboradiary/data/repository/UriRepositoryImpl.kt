package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.data.uri.PersistableUriPermissionOperationFailureException
import com.websarva.wings.android.zuboradiary.domain.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.PermissionException

internal class UriRepositoryImpl (
    private val dataSource: UriPermissionDataSource
) : UriRepository {

    override fun takePersistableUriPermission(uriString: String) {
        val uri = Uri.parse(uriString)
        try {
            dataSource.takePersistableUriPermission(uri)
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw PermissionException(cause = e)
        }
    }

    override fun releasePersistableUriPermission(uriString: String) {
        val uri = Uri.parse(uriString)
        try {
            dataSource.releasePersistableUriPermission(uri)
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw PermissionException(cause = e)
        }
    }

    override fun releaseAllPersistableUriPermission() {
        try {
            dataSource.releaseAllPersistableUriPermission()
        } catch (e: PersistableUriPermissionOperationFailureException) {
            throw PermissionException(cause = e)
        }
    }
}
