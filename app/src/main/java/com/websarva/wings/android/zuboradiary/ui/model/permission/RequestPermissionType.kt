package com.websarva.wings.android.zuboradiary.ui.model.permission

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert.PermissionRationaleDialogFragment
import kotlinx.parcelize.Parcelize

/**
 * 権限要求の種類を識別するためのenum。
 *
 * 権限ダイアログ（[PermissionRationaleDialogFragment]）の結果をどの権限要求に関連付けるかを判断する際に使用する。
 */
@Parcelize
internal enum class RequestPermissionType : Parcelable {
    POST_NOTIFICATIONS,
    ACCESS_LOCATION
}
