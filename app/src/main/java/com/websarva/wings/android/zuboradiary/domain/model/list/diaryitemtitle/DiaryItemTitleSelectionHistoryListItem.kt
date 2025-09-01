package com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle

/**
 * 日記項目のタイトル選択履歴の各アイテムを表すデータクラス。
 *
 * このクラスは、ユーザーが過去に入力または選択した日記項目のタイトルを保持する。
 *
 * @property title 選択された、または入力された日記項目のタイトル。
 */
internal data class DiaryItemTitleSelectionHistoryListItem(
    val title: String
)
