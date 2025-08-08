package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class DiaryListLoadFailureException (
    cause: Throwable
) : DomainException("日記リストの読込に失敗しました。", cause)
