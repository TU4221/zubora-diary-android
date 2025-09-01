package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalDate

/**
 * 特定の日付の日記の存在確認処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param date 存在確認をしようとした日記の日付。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class DiaryExistenceCheckFailureException(
    date: LocalDate,
    cause: Throwable
) : DomainException("日付 '$date' の日記の存在確認に失敗しました。", cause)
