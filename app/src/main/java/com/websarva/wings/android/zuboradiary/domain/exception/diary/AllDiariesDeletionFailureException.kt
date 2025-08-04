package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class AllDiariesDeletionFailureException (
    cause: Throwable
    ) : DomainException("すべての日記の削除に失敗しました。", cause)
