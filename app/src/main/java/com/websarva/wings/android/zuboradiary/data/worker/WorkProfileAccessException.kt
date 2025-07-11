package com.websarva.wings.android.zuboradiary.data.worker

internal class WorkProfileAccessException (
    cause: Throwable? = null
) : Exception("仕事用プロファイルへのアクセスまたは操作に失敗しました。", cause)
