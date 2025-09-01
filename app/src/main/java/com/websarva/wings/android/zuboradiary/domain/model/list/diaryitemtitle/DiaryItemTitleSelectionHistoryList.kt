package com.websarva.wings.android.zuboradiary.domain.model.list.diaryitemtitle

/**
 * 日記項目のタイトル選択履歴のリストを保持するデータクラス。
 *
 * このクラスは、[DiaryItemTitleSelectionHistoryListItem] のリストをカプセル化。
 *
 * @property itemList [DiaryItemTitleSelectionHistoryListItem] のリスト。
 */
internal data class DiaryItemTitleSelectionHistoryList(
    val itemList: List<DiaryItemTitleSelectionHistoryListItem>
) {

    /**
     * 履歴リストが空かどうかを示す。
     *
     * @return リストが空の場合は `true`、そうでない場合は `false`。
     */
    val isEmpty = itemList.isEmpty()
}
