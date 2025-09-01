package com.websarva.wings.android.zuboradiary.domain.exception.uri

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * アプリが保持している全ての永続的なURI権限の解放処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class AllPersistableUriPermissionReleaseFailureException(
    cause: Throwable
) : DomainException("全ての永続的URI権限の解放に失敗しました。", cause)
