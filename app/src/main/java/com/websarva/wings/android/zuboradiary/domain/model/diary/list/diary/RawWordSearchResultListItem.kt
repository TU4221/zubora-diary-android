package com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.domain.model.serializer.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
import java.time.LocalDate

/**
 * 単語検索結果の生データを表すデータクラス。
 *
 * このクラスは、データベースから取得した検索結果を直接的にマッピングすることを目的としており、
 * アプリケーションのドメイン層で運用される[DiaryDayListItem.WordSearchResult]に変換される。
 * 最大5つの日記項目タイトルとコメントのペアを保持できる構造になっている。
 *
 * @property id 日記のID。
 * @property date 日記の日付。
 * @property title 日記全体のタイトル。
 * @property item1Title 1番目のアイテムのタイトル。
 * @property item1Comment 1番目のアイテムのコメント。
 * @property item2Title 2番目のアイテムのタイトル。未記述の場合null。
 * @property item2Comment 2番目のアイテムのコメント。未記述の場合null。
 * @property item3Title 3番目のアイテムのタイトル。未記述の場合null。
 * @property item3Comment 3番目のアイテムのコメント。未記述の場合null。
 * @property item4Title 4番目のアイテムのタイトル。未記述の場合null。
 * @property item4Comment 4番目のアイテムのコメント。未記述の場合null。
 * @property item5Title 5番目のアイテムのタイトル。未記述の場合null。
 * @property item5Comment 5番目のアイテムのコメント。未記述の場合null。
 */
@Serializable
internal data class RawWordSearchResultListItem(
    val id: DiaryId,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val title: DiaryTitle,
    val item1Title: DiaryItemTitle,
    val item1Comment: DiaryItemComment,
    val item2Title: DiaryItemTitle?,
    val item2Comment: DiaryItemComment?,
    val item3Title: DiaryItemTitle?,
    val item3Comment: DiaryItemComment?,
    val item4Title: DiaryItemTitle?,
    val item4Comment: DiaryItemComment?,
    val item5Title: DiaryItemTitle?,
    val item5Comment: DiaryItemComment?
) : JavaSerializable
