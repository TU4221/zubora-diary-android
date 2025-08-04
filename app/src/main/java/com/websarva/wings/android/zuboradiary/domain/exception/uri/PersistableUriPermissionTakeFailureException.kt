package com.websarva.wings.android.zuboradiary.domain.exception.uri

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class PersistableUriPermissionTakeFailureException(
    uri: Uri,
    cause: Throwable
) : DomainException("URI '$uri' の永続的URI権限の取得に失敗しました。", cause)
