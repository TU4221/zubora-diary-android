package com.websarva.wings.android.zuboradiary.ui.model.diary.item

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryEditFragment
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen.DiaryItemTitleEditDialog
import kotlinx.parcelize.Parcelize

/**
 * 日記項目のタイトル編集結果を表すUIモデル。
 *
 * ユーザーが項目タイトルを編集するときに、
 * 現在の項目タイトルの情報を[DiaryEditFragment]から[DiaryItemTitleEditDialog]へ渡すために使用される。
 *
 * また、ユーザーが編集を完了したときに、
 * 編集した項目タイトルの情報を[DiaryItemTitleEditDialog]から[DiaryEditFragment]へ渡すために使用される。
 *
 *
 * @property itemNumber 対象となる日記項目の番号。
 * @property id 日記項目タイトル選択履歴の一意なID。項目タイトルが未編集、又は選択履歴から選択されてない場合は`null`。
 * @property title 日記項目のタイトル文字列。
 */
@Parcelize
data class DiaryItemTitleSelectionUi(
    val itemNumber: Int,
    val id: String?,
    val title: String,
) : Parcelable
