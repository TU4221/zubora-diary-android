package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionTakeFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.AllPersistableUriPermissionReleaseFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.uri.PersistableUriPermissionReleaseFailureException

/**
 * URIの永続的なパーミッション管理を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、URIに対する永続的なアクセス許可の取得と解放機能を提供します。
 *
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外 ([DomainException] のサブクラス) をスローします。
 */
internal interface UriRepository {

    /**
     * 指定されたURI文字列に対する永続的なアクセス許可を取得する。
     *
     * @param uriString アクセス許可を取得する対象のURI文字列。
     * @throws PersistableUriPermissionTakeFailureException 永続的なURIパーミッションの取得に失敗した場合。
     */
    fun takePersistableUriPermission(uriString: String)

    /**
     * 指定されたURI文字列に対する永続的なアクセス許可を解放する。
     *
     * @param uriString アクセス許可を解放する対象のURI文字列。
     * @throws PersistableUriPermissionReleaseFailureException 永続的なURIパーミッションの解放に失敗した場合。
     */
    fun releasePersistableUriPermission(uriString: String)

    /**
     * アプリケーションが保持しているすべての永続的なURIアクセス許可を解放する。
     *
     * @throws AllPersistableUriPermissionReleaseFailureException 全ての永続的なURIパーミッションの解放に失敗した場合。
     */
    fun releaseAllPersistableUriPermission()
}
