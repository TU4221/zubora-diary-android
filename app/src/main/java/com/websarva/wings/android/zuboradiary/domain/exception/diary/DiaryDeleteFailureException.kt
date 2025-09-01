package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalDate

/**
 * 特定の日付の日記の削除処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param date 削除しようとした日記の日付。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class DiaryDeleteFailureException (
    date: LocalDate,
    cause: Throwable
    ) : DomainException("日付 '$date' の日記の削除に失敗しました。", cause)
