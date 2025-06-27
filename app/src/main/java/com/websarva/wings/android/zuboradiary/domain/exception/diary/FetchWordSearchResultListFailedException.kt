package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class FetchWordSearchResultListFailedException (
    searchWord: String,
    cause: Throwable
) : DomainException("'$searchWord' の検索結果の取得に失敗しました。", cause)
