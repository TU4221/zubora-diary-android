package com.websarva.wings.android.zuboradiary.domain.model.list.diary

import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.ItemNumber
import java.time.LocalDate

/**
 * 日記リストの各日を表すアイテムの基底クラス。
 *
 * このクラスは、日記リスト内で日付を持つアイテムの共通の型を提供する。
 * 具体的なアイテムの種類（例: 通常の日記表示、検索結果表示）に応じて、
 * このクラスを継承したサブクラスが定義される。
 *
 * @property date このリストアイテムが表す日付。
 */
internal sealed class DiaryDayListItem(
    open val date: LocalDate
) {

    /**
     * 標準的な日記リストアイテム。
     * 日付、タイトル、画像URIを持つ。
     *
     * @property date 日記の日付。
     * @property title 日記のタイトル。
     * @property imageFileName 日記に関連付けられた画像ファイル名。画像がない場合はnull。
     */
    data class Standard(
        override val date: LocalDate,
        val title: String,
        val imageFileName: ImageFileName?
    ) : DiaryDayListItem(date)

    /**
     * 単語検索結果として表示される日記リストアイテム。
     * 日付、日記タイトル、日記項目番号、日記項目タイトル、日記項目コメント、および検索語を持つ。
     * (検索単語が日記タイトルのみに含まれる場合は保持する日記項目は項目1となる)
     *
     * @property date 検索にヒットした日記の日付。
     * @property title 検索にヒットした日記のタイトル。
     * @property itemNumber 検索にヒットした日記項目の番号。
     *                      (検索にヒットした日記の全ての項目に検索単語が含まれない場合は日記項目1となる)
     * @property itemTitle 検索にヒットした日記項目のタイトル。
     *                     (検索にヒットした日記の全ての項目に検索単語が含まれない場合は日記項目1のタイトルとなる)
     * @property itemComment 検索にヒットした日記項目のコメント。
     *                       (検索にヒットした日記の全ての項目に検索単語が含まれない場合は日記項目1のコメントとなる)
     * @property searchWord このアイテムがヒットした検索単語。
     */
    data class WordSearchResult(
        override val date: LocalDate,
        val title: String,
        val itemNumber: ItemNumber,
        val itemTitle: String,
        val itemComment: String,
        val searchWord: String,
    ) : DiaryDayListItem(date)
}
