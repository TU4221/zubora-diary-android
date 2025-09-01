package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 日記項目のタイトル選択履歴から特定の項目を削除する処理中に
 * 予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param title 削除しようとした日記項目のタイトル。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class DiaryItemTitleSelectionHistoryItemDeleteFailureException (
    title: String,
    cause: Throwable
) : DomainException("日記項目タイトル選択履歴の '$title' の削除に失敗しました。", cause)
