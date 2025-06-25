package com.websarva.wings.android.zuboradiary.data.preferences

import com.websarva.wings.android.zuboradiary.data.model.DataException

internal class UserPreferencesAccessException (
    cause: Throwable? = null
) : DataException("ユーザー情報へのアクセスに失敗しました。", cause)
