package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class WordSearchResultListLoadFailureException (
    searchWord: String,
    cause: Throwable
) : DomainException("'$searchWord' の検索結果の読込に失敗しました。", cause)
