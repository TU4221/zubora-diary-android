package com.websarva.wings.android.zuboradiary.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 日記項目のタイトル選択履歴を表すRoomエンティティクラス。
 *
 * このクラスは、ユーザーが過去に選択した日記項目のタイトルと選択した日時の履歴を保持する。
 * [title] カラムにはユニーク制約が設定されており、同じタイトルが複数保存されることはない。
 *
 * @property id 識別番号。このエンティティの主キー。
 * @property title 選択した日記項目のタイトル。
 * @property log 選択した日時。
 */
@Entity(
    tableName = "diary_item_title_selection_history",
    indices = [Index(value = ["title"], unique = true)]
)
internal data class DiaryItemTitleSelectionHistoryEntity (
    @PrimaryKey
    val id: String,
    val title: String,
    val log: LocalDateTime
)
