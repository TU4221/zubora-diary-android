package com.websarva.wings.android.zuboradiary.domain.exception.location

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class ObtainCurrentLocationFailedException(
    cause: Throwable
) : DomainException("現在位置の取得に失敗しました。", cause)
