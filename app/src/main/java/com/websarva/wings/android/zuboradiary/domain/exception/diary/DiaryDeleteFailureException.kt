package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalDate

internal class DiaryDeleteFailureException (
    date: LocalDate,
    cause: Throwable
    ) : DomainException("日付 '$date' の日記の削除に失敗しました。", cause)
