package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class DeleteAllDataFailedException (
    cause: Throwable
    ) : DomainException("すべての日記データの削除に失敗しました。", cause)
