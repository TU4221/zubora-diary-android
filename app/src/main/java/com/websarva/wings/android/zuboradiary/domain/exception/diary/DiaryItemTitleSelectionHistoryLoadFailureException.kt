package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 日記項目のタイトル選択履歴の読み込み処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class DiaryItemTitleSelectionHistoryLoadFailureException (
    cause: Throwable
) : DomainException("日記項目タイトル選択履歴の読込に失敗しました。", cause)
