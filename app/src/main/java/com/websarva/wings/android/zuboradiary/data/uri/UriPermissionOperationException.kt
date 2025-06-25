package com.websarva.wings.android.zuboradiary.data.uri

import com.websarva.wings.android.zuboradiary.data.model.DataException

internal class UriPermissionOperationException (
    cause: Throwable? = null
) : DataException("Uri権限の操作に失敗しました。", cause)
