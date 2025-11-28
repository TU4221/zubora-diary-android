package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.permission.RequestPermissionType

/** [RequestPermissionType]に対応する文字列リソースIDを返す。 */
private val RequestPermissionType.stringResId: Int
    get() = when (this) {
        RequestPermissionType.POST_NOTIFICATIONS -> R.string.fragment_settings_permission_name_notification
        RequestPermissionType.ACCESS_LOCATION -> R.string.fragment_settings_permission_name_location
    }

/**
 * [RequestPermissionType]を、ユーザーに表示するためのローカライズされた文字列に変換する。
 * @param context 文字列リソースを取得するためのコンテキスト。
 */
internal fun RequestPermissionType.asString(context: Context): String {
    return context.getString(this.stringResId)
}
