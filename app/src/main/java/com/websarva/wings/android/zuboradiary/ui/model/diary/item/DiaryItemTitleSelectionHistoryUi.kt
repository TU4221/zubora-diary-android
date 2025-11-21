package com.websarva.wings.android.zuboradiary.ui.model.diary.item

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

/**
 * 日記項目タイトルの選択履歴を表すUIモデル。
 *
 * このクラスは、日記項目選択履歴に保存する日記項目のタイトルとその編集日時を保持する。
 *
 * @property id 履歴の一意な識別子。
 * @property title 履歴として保存するタイトル文字列。
 * @property log このタイトルに編集した日時。
 */
@Parcelize
data class DiaryItemTitleSelectionHistoryUi(
    override val id: String,
    val title: String,
    val log: LocalDateTime
) : Parcelable, Identifiable
