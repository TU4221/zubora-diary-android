package com.websarva.wings.android.zuboradiary.domain.exception.uri

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class RevokePersistentAccessAllUriFailedException(
    cause: Throwable
) : DomainException("すべてのUriの永続的なアクセス権の取り消しに失敗しました。", cause)
