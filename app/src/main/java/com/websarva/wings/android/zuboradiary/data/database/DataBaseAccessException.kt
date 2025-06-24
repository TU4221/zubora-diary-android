package com.websarva.wings.android.zuboradiary.data.database

import com.websarva.wings.android.zuboradiary.data.model.DataException

internal class DataBaseAccessException (
    cause: Throwable? = null
) : DataException("データベースへのアクセスに失敗しました。", cause)
