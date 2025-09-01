package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 日記リストの読み込み処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class DiaryListLoadFailureException (
    cause: Throwable
) : DomainException("日記リストの読込に失敗しました。", cause)
