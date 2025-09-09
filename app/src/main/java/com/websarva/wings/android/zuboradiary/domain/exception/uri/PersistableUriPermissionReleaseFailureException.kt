package com.websarva.wings.android.zuboradiary.domain.exception.uri

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException

// TODO:削除
/**
 * 指定されたURIに対する永続的なアクセス権限の付与処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param uriString 権限付与対象のURI文字列。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class PersistableUriPermissionReleaseFailureException(
    uriString: String,
    cause: Throwable
) : UseCaseException("URI '$uriString' の永続的URI権限の解放に失敗しました。", cause)
