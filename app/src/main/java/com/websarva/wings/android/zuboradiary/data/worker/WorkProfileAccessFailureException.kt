package com.websarva.wings.android.zuboradiary.data.worker

/**
 * 仕事用プロファイルへのアクセスまたは操作に失敗した場合にスローされる例外クラス。
 *
 * 仕事用プロファイルへのアクセスまたは操作に何らかの問題が発生した場合に使用する。
 *
 * @param cause 根本原因となったThrowable。
 */
internal class WorkProfileAccessFailureException (
    cause: Throwable
) : Exception("仕事用プロファイルへのアクセスまたは操作に失敗しました。", cause)
