package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class LoadDiaryListFailedException (
    cause: Throwable
) : DomainException("日記リストの読み込みに失敗しました。", cause)
