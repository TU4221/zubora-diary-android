package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.collections.isNotEmpty

/**
 * 日記一覧のリスト全体を表すUIモデル。
 *
 * [DiaryListItemUi]のリストを保持し、リストが空かどうかの判定や、
 * 含まれる日記アイテム数をカウントするためのユーティリティを提供する。
 *
 * @param T [DiaryListItemContainerUi]を継承する、日記リストアイテムの具体的なデータコンテナの型。
 * @property itemList ヘッダーや日記アイテムなど、リストに表示される全ての要素を格納するリスト。
 */
@Parcelize
data class DiaryListUi<T: DiaryListItemContainerUi>(
    val itemList: List<DiaryListItemUi<T>> = emptyList()
) : Parcelable {

    /** リストが空であるかどうかを示す。 */
    val isEmpty get() = itemList.isEmpty()

    /** リストが空でないかどうかを示す。 */
    val isNotEmpty get() = itemList.isNotEmpty()

    /**
     * リスト内に含まれる日記アイテムの数を返す。
     */
    fun countDiaries(): Int {
        return itemList.filterIsInstance<DiaryListItemUi.Diary<T>>().count()
    }
}
