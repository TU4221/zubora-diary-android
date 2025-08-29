package com.websarva.wings.android.zuboradiary.data.database

/**
 * データベースアクセス失敗時にスローする例外クラス。
 *
 * データベースへの読み書き操作中に何らかの問題が発生した場合に使用する。
 *
 * @param cause データベースアクセス失敗の根本原因となったThrowable。
 */
internal class DataBaseAccessFailureException (
    cause: Throwable
) : Exception("データベースへのアクセスに失敗しました。", cause)
