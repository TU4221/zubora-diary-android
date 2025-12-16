package com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 日記項目タイトルの選択履歴リストを表すUIモデル。
 *
 * [DiaryItemTitleSelectionHistoryListItemUi]のリストを保持し、リストが空かどうかの判定機能を提供する。
 *
 * @property itemList 履歴リストのアイテムを格納するリスト。
 */
@Parcelize
data class DiaryItemTitleSelectionHistoryListUi(
    val itemList: List<DiaryItemTitleSelectionHistoryListItemUi>
) : Parcelable {

    /** リストが空であるかどうかを示す。 */
    val isEmpty
        get() = itemList.isEmpty()
}
