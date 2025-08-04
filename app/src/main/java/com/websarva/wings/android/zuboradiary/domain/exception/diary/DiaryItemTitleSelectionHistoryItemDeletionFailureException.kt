package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class DiaryItemTitleSelectionHistoryItemDeletionFailureException (
    title: String,
    cause: Throwable
) : DomainException("日記項目タイトル選択履歴の '$title' の削除に失敗しました。", cause)
