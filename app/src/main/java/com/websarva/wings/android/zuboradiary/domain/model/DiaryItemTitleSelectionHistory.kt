package com.websarva.wings.android.zuboradiary.domain.model

import java.time.LocalDateTime

/**
 * 日記項目のタイトル選択履歴を表すデータクラス。
 *
 * このクラスは、ユーザーが過去に日記項目のタイトルとして選択または入力した文字列と、
 * その操作が行われた日時を記録する。
 *
 * @property id 識別番号。
 * @property title 選択または入力された日記項目のタイトル。
 * @property log そのタイトルが選択または入力された日時。
 */
internal data class DiaryItemTitleSelectionHistory (
    val id: DiaryItemTitleSelectionHistoryId,
    val title: String,
    val log: LocalDateTime
)
