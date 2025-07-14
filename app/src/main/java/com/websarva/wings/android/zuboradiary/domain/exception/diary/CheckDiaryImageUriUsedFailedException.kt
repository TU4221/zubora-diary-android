package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class CheckDiaryImageUriUsedFailedException(
    uriString: String,
    cause: Throwable
) : DomainException("画像URI '$uriString' の使用確認に失敗しました。", cause)
