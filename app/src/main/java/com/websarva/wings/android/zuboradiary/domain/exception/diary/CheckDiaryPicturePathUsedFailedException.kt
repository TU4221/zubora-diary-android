package com.websarva.wings.android.zuboradiary.domain.exception.diary

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class CheckDiaryPicturePathUsedFailedException(
    uri: Uri,
    cause: Throwable
) : DomainException("画像URI '$uri' の使用確認に失敗しました。", cause)
