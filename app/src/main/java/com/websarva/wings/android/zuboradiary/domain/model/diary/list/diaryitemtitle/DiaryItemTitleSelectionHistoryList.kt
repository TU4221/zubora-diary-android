package com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * 日記項目のタイトル選択履歴のリストを保持するデータクラス。
 *
 * このクラスは、[DiaryItemTitleSelectionHistoryListItem] のリストをカプセル化。
 *
 * @property itemList [DiaryItemTitleSelectionHistoryListItem] のリスト。
 */
@Serializable
internal data class DiaryItemTitleSelectionHistoryList(
    val itemList: List<DiaryItemTitleSelectionHistoryListItem>
) : JavaSerializable {

    /**
     * 履歴リストが空かどうかを示す。
     *
     * @return リストが空の場合は `true`、そうでない場合は `false`。
     */
    val isEmpty = itemList.isEmpty()
}
