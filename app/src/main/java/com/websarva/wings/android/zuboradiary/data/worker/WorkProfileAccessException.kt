package com.websarva.wings.android.zuboradiary.data.worker

import com.websarva.wings.android.zuboradiary.data.model.DataException

internal class WorkProfileAccessException (
    cause: Throwable? = null
) : DataException("仕事用プロファイルへのアクセスまたは操作に失敗しました。", cause)
