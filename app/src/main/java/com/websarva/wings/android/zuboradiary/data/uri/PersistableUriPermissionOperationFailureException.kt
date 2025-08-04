package com.websarva.wings.android.zuboradiary.data.uri

internal class PersistableUriPermissionOperationFailureException (
    cause: Throwable? = null
) : Exception("Uri権限の操作に失敗しました。", cause)
