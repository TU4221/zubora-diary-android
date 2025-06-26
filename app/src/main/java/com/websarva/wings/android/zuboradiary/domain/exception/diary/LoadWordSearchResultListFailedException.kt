package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class LoadWordSearchResultListFailedException (
    searchWord: String,
    cause: Throwable
) : DomainException("'$searchWord' の検索結果の読み込みに失敗しました。", cause)
