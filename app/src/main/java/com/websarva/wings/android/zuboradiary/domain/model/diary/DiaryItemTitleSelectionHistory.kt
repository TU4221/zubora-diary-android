package com.websarva.wings.android.zuboradiary.domain.model.diary

import com.websarva.wings.android.zuboradiary.core.serializer.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
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
@Serializable
internal data class DiaryItemTitleSelectionHistory (
    val id: DiaryItemTitleSelectionHistoryId,
    val title: DiaryItemTitle,
    @Serializable(with = LocalDateTimeSerializer::class)
    val log: LocalDateTime
) : JavaSerializable
