package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 指定された検索ワードに一致する日記のリストの読み込み処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param searchWord 検索に使用されたキーワード。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class WordSearchResultListLoadFailureException (
    searchWord: String,
    cause: Throwable
) : DomainException("'$searchWord' の検索結果の読込に失敗しました。", cause)
