package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 指定された画像URIが他の日記で使用されているかどうかの確認処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param uriString 確認対象の画像URIの文字列。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class DiaryImageUriUsageCheckFailureException(
    uriString: String,
    cause: Throwable
) : DomainException("画像URI '$uriString' の使用確認に失敗しました。", cause)
