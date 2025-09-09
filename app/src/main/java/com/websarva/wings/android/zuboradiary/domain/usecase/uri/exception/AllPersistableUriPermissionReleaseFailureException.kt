package com.websarva.wings.android.zuboradiary.domain.usecase.uri.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException

// TODO:URI関係はレポジトリ層までとする為、このクラスは削除
/**
 * アプリが保持している全ての永続的なURI権限の解放処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class AllPersistableUriPermissionReleaseFailureException(
    cause: Throwable
) : UseCaseException("全ての永続的URI権限の解放に失敗しました。", cause)
