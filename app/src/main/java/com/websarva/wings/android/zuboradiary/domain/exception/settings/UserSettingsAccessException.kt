package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class UserSettingsAccessException (
    cause: Throwable? = null
) : DomainException("ユーザー設定へのアクセスに失敗しました。", cause)
