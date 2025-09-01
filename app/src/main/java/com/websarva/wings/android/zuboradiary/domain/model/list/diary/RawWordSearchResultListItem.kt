package com.websarva.wings.android.zuboradiary.domain.model.list.diary

import java.time.LocalDate

/**
 * 単語検索結果の生データを表すデータクラス。
 *
 * このクラスは、データベースから取得した検索結果を直接的にマッピングすることを目的としており、
 * アプリケーションのドメイン層で運用される[DiaryDayListItem.WordSearchResult]に変換される。
 * 最大5つの日記項目タイトルとコメントのペアを保持できる構造になっている。
 *
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
internal data class RawWordSearchResultListItem(
    val date: LocalDate,
    val title: String,
    val item1Title: String,
    val item1Comment: String,
    val item2Title: String?,
    val item2Comment: String?,
    val item3Title: String?,
    val item3Comment: String?,
    val item4Title: String?,
    val item4Comment: String?,
    val item5Title: String?,
    val item5Comment: String?
) // TODO:初期化ブロックでタイトルとコメントがペア(両方nullor非null)になっているか確認
