package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class DiaryListFetchFailureException (
    cause: Throwable
) : DomainException("日記リストの取得に失敗しました。", cause)
