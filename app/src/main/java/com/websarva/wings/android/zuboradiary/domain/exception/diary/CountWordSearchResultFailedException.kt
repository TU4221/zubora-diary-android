package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class CountWordSearchResultFailedException(
    searchWord: String,
    cause: Throwable
) : DomainException("検索ワード '$searchWord' に一致する日記の総数の取得に失敗しました。", cause)
