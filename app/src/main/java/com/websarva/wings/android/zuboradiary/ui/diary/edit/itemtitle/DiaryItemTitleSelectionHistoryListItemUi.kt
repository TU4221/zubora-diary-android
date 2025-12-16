package com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.common.model.Identifiable
import kotlinx.parcelize.Parcelize

/**
 * 日記項目タイトルの選択履歴リストのアイテムを表すUIモデル。
 *
 * 日記項目タイトル編集画面([DiaryItemTitleEditDialog])で、
 * ユーザーが過去に使用したタイトルを選択するために使用される。
 *
 * @property id 履歴の一意な識別子。
 * @property title 履歴として保存されているタイトル文字列。
 */
@Parcelize
data class DiaryItemTitleSelectionHistoryListItemUi(
    override val id: String,
    val title: String
) : Parcelable, Identifiable
