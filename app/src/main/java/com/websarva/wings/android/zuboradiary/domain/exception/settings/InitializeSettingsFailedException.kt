package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class InitializeSettingsFailedException(
    cause: Throwable
) : DomainException("設定の初期化に失敗しました。", cause)
