package com.websarva.wings.android.zuboradiary.di.data.common

import android.content.Context
import android.os.Build
import com.websarva.wings.android.zuboradiary.data.common.PermissionChecker
import com.websarva.wings.android.zuboradiary.ui.utils.isPostNotificationsGranted

internal class PermissionCheckerImpl(
    private val context: Context
): PermissionChecker {

    override val isPostNotificationsGranted:Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.isPostNotificationsGranted()
            } else {
                true
            }
        }
}
