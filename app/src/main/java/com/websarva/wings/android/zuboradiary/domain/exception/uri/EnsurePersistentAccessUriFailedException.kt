package com.websarva.wings.android.zuboradiary.domain.exception.uri

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class EnsurePersistentAccessUriFailedException(
    uri: Uri,
    cause: Throwable
) : DomainException("Uri '$uri' の永続的なアクセス権の確保に失敗しました。", cause)
