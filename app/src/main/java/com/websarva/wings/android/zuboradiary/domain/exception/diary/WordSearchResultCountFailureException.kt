package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 指定された検索ワードに一致する日記の総数を取得する処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param searchWord 検索に使用されたキーワード。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class WordSearchResultCountFailureException(
    searchWord: String,
    cause: Throwable
) : DomainException("検索ワード '$searchWord' に一致する日記の総数の取得に失敗しました。", cause)
