package com.websarva.wings.android.zuboradiary.data.database

/**
 * データベースアクセス失敗時にスローする例外クラス。
 *
 * データベースへの読み書き操作中に何らかの問題が発生した場合に使用する。
 *
 * @param cause データベースアクセス失敗の根本原因となったThrowable (オプショナル)。
 */
internal class DataBaseAccessFailureException (
    cause: Throwable? = null //TODO:非Null型に変更
) : Exception("データベースへのアクセスに失敗しました。", cause)
