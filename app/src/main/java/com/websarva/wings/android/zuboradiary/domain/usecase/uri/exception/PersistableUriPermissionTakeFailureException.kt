package com.websarva.wings.android.zuboradiary.domain.usecase.uri.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException

// TODO:URI関係はレポジトリ層までとする為、このクラスは削除
/**
 * 指定されたURIに対する永続的なアクセス権限の解放処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param uriString 権限解放対象のURI文字列。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class PersistableUriPermissionTakeFailureException(
    uriString: String,
    cause: Throwable
) : UseCaseException("URI '$uriString' の永続的URI権限の取得に失敗しました。", cause)
