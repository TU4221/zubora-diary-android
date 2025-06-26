package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class LoadDiaryItemTitleSelectionHistoryFailedException (
    cause: Throwable
) : DomainException("日記項目タイトル選択履歴の読み込みに失敗しました。", cause)
