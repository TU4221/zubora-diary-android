package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 日記の総数を取得する処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class DiaryCountFailureException(
    cause: Throwable
) : DomainException("日記の総数の取得に失敗しました。", cause)
