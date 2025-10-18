package com.websarva.wings.android.zuboradiary.domain.model.diary.list.diaryitemtitle

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * 日記項目のタイトル選択履歴の各アイテムを表すデータクラス。
 *
 * このクラスは、ユーザーが過去に入力または選択した日記項目のタイトルを保持する。
 *
 * @property id 識別番号。
 * @property title 選択された、または入力された日記項目のタイトル。
 */
@Serializable
internal data class DiaryItemTitleSelectionHistoryListItem(
    val id: DiaryItemTitleSelectionHistoryId,
    val title: DiaryItemTitle
) : JavaSerializable
