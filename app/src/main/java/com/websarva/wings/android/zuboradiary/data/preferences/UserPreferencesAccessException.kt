package com.websarva.wings.android.zuboradiary.data.preferences

import com.websarva.wings.android.zuboradiary.data.model.DataException

internal class UserPreferencesAccessException (
    cause: Throwable? = null
) : DataException("ユーザー設定へのアクセスに失敗しました。", cause)
