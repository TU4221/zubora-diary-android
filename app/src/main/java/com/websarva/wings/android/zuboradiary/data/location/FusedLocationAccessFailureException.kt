package com.websarva.wings.android.zuboradiary.data.location

/**
 * Fused Location Provider APIによる位置情報取得失敗時にスローする例外クラス。
 *
 * 位置情報の取得処理中に何らかの問題が発生した場合に使用する。
 *
 * @param cause 位置情報取得失敗の根本原因となったThrowable。
 */
internal class FusedLocationAccessFailureException (
    cause: Throwable
) : Exception("位置情報の取得に失敗しました。", cause)
