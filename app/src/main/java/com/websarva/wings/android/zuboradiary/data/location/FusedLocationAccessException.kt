package com.websarva.wings.android.zuboradiary.data.location

import com.websarva.wings.android.zuboradiary.data.model.DataException

internal class FusedLocationAccessException (
    cause: Throwable? = null
) : DataException("位置情報の取得に失敗しました。", cause)
