package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class DiaryItemTitleSelectionHistoryLoadFailureException (
    cause: Throwable
) : DomainException("日記項目タイトル選択履歴の読込に失敗しました。", cause)
