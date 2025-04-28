package com.websarva.wings.android.zuboradiary.ui.permission

import android.net.Uri

sealed class UriPermissionAction {
    data class Take(val uri: Uri): UriPermissionAction()
    data class Release(val uri: Uri) : UriPermissionAction()
    data class ReleaseAndTake(val releaseUri: Uri, val takeUri: Uri) : UriPermissionAction()
    data object AllRelease : UriPermissionAction()
    data object None: UriPermissionAction()
}
