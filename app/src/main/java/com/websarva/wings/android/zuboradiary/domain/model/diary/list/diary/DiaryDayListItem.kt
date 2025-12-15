package com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.diary.SearchWord
import java.time.LocalDate

/**
 * 日記リストの各日を表すアイテム。
 *
 * このクラスは、日記リスト内で日付を持つアイテムの共通の型を提供する。
 * 具体的なアイテムの種類（例: 通常の日記表示、検索結果表示）に応じて、
 * このクラスを継承したサブクラスが定義される。
 *
 * @property id このリストアイテムが表す日記ID。
 * @property date このリストアイテムが表す日付。
 */
internal sealed interface DiaryDayListItem {

    /** @property id このリストアイテムが表す日記ID。 */
    val id: DiaryId

    /** @property date このリストアイテムが表す日付。 */
    val date: LocalDate

    /**
     * 標準的な日記リストアイテム。
     * 日付、タイトル、画像URIを持つ。
     *
     * @property id 日記のID。
     * @property date 日記の日付。
     * @property title 日記のタイトル。
     * @property imageFileName 日記に関連付けられた画像ファイル名。画像がない場合はnull。
     */
    data class Standard(
        override val id: DiaryId,
        override val date: LocalDate,
        val title: DiaryTitle,
        val imageFileName: DiaryImageFileName?
    ) : DiaryDayListItem

    /**
     * 単語検索結果として表示される日記リストアイテム。
     * 日付、日記タイトル、日記項目番号、日記項目タイトル、日記項目コメント、および検索語を持つ。
     * (検索単語が日記タイトルのみに含まれる場合は保持する日記項目は項目1となる)
     *
     * @property id 検索にヒットした日記のID。
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
        override val id: DiaryId,
        override val date: LocalDate,
        val title: DiaryTitle,
        val itemNumber: DiaryItemNumber,
        val itemTitle: DiaryItemTitle,
        val itemComment: DiaryItemComment,
        val searchWord: SearchWord,
    ) : DiaryDayListItem
}
