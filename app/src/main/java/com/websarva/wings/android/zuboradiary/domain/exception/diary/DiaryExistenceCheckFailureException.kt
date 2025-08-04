package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalDate

internal class DiaryExistenceCheckFailureException(
    date: LocalDate,
    cause: Throwable
) : DomainException("日付 '$date' の日記の存在確認に失敗しました。", cause)
