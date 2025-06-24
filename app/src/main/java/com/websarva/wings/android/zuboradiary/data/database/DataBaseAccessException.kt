package com.websarva.wings.android.zuboradiary.data.database

internal class DataBaseAccessException (
    cause: Throwable? = null
) : Exception("データベースへのアクセスに失敗しました。", cause)
