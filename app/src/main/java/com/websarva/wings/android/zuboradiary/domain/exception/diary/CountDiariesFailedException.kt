package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class CountDiariesFailedException(
    cause: Throwable
) : DomainException("日記の総数の取得に失敗しました。", cause)
