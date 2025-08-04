package com.websarva.wings.android.zuboradiary.data.location

internal class FusedLocationAccessFailureException (
    cause: Throwable? = null
) : Exception("位置情報の取得に失敗しました。", cause)
