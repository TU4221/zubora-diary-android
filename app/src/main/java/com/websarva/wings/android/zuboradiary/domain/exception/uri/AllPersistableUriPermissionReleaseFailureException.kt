package com.websarva.wings.android.zuboradiary.domain.exception.uri

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class AllPersistableUriPermissionReleaseFailureException(
    cause: Throwable
) : DomainException("全ての永続的URI権限の解放に失敗しました。", cause)
