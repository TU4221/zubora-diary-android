package com.websarva.wings.android.zuboradiary.domain.exception.uri

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class RevokePersistentAccessUriFailedException(
    uri: Uri,
    cause: Throwable
) : DomainException("Uri '$uri' の永続的なアクセス権の取り消しに失敗しました。", cause)
