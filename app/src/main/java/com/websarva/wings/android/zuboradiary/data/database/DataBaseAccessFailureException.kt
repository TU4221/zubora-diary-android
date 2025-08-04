package com.websarva.wings.android.zuboradiary.data.database

internal class DataBaseAccessFailureException (
    cause: Throwable? = null
) : Exception("データベースへのアクセスに失敗しました。", cause)
