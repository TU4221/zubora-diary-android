package com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 日記項目編集ダイアログへの遷移時に渡すパラメータ。
 *
 * Navigation Componentの引数として使用されることを想定している。
 *
 * @property resultKey ダイアログの結果を返すためのユニークなキー。
 * @property diaryItemTitleSelection 日記項目のタイトル編集対象データ。
 */
@Parcelize
data class DiaryItemTitleEditDialogParams(
    val resultKey: String,
    val diaryItemTitleSelection: DiaryItemTitleSelectionUi,
) : Parcelable
