package com.websarva.wings.android.zuboradiary.domain.exception.uri

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 指定されたURIに対する永続的なアクセス権限の解放処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param uri 権限解放対象のURI。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class PersistableUriPermissionTakeFailureException(
    uri: Uri,
    cause: Throwable
) : DomainException("URI '$uri' の永続的URI権限の取得に失敗しました。", cause)
