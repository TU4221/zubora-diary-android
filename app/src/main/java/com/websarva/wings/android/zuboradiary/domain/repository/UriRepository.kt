package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionTakeFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.AllPersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionReleaseFailureException

internal interface UriRepository {

    @Throws(PersistableUriPermissionTakeFailureException::class)
    fun takePersistableUriPermission(uriString: String)

    @Throws(PersistableUriPermissionReleaseFailureException::class)
    fun releasePersistableUriPermission(uriString: String)

    @Throws(AllPersistableUriPermissionReleaseFailureException::class)
    fun releaseAllPersistableUriPermission()
}
