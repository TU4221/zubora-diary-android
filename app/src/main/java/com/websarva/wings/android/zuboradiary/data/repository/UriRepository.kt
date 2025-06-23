package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.error.UriError

internal class UriRepository (
    private val dataSource: UriPermissionDataSource
) {

    @Throws(UriError.TakePermission::class)
    fun takePersistablePermission(uri: Uri) {
        try {
            dataSource.takePersistablePermission(uri)
        } catch (e: RuntimeException) {
            throw UriError.TakePermission(e)
        }
    }

    @Throws(UriError.ReleasePermission::class)
    fun releasePersistablePermission(uri: Uri) {
        dataSource.releasePersistablePermission(uri)
        try {
            dataSource.takePersistablePermission(uri)
        } catch (e: RuntimeException) {
            throw UriError.ReleasePermission(e)
        }
    }

    // TODO:UseCaseで処理するように変更
    fun releaseAllPersistablePermission() {
        dataSource.releaseAllPersistablePermission()
    }
}
