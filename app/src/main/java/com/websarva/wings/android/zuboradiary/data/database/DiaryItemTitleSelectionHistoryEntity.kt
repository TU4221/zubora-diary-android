package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 日記項目のタイトル選択履歴を表すRoomエンティティクラス。
 *
 * このクラスは、ユーザーが過去に選択した日記項目のタイトルと選択した日時の履歴を保持する。
 *
 * @property title 選択した日記項目のタイトル。このエンティティの主キー。
 * @property log 選択した日時。
 */
@Entity(tableName = "diary_item_title_selection_history")
internal data class DiaryItemTitleSelectionHistoryEntity (
    @PrimaryKey
    val title: String,
    val log: String
)
