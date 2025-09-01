package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 特定の日記の読み込み処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param message 例外の詳細メッセージ。通常、どの日の日記の読み込みに失敗したかなどの情報を含む。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class DiaryLoadFailureException (
    message: String,
    cause: Throwable
) : DomainException(message, cause)
