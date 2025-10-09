package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.WordSearchResultListLoadException
import com.websarva.wings.android.zuboradiary.domain.mapper.toDiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.RawWordSearchResultListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.SearchWord
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 指定された検索ワードに一致する日記のリストを読み込み、表示用のデータに整形して返すユースケース。
 *
 * 取得した生の結果リストから、検索ワードにヒットした具体的な項目を抽出し、
 * 年月ごとにグループ化された表示用のリスト形式に変換する。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class LoadWordSearchResultListUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "ワード検索結果リスト読込_"

    private val itemNumberKey = "ItemNumber"
    private val itemTitleKey = "ItemTitle"
    private val itemCommentKey = "ItemComment"

    /**
     * ユースケースを実行し、指定された検索ワードに一致する日記のリストを読み込み、整形して返す。
     *
     * @param numLoadItems 一度に読み込むアイテム数。1以上の値を指定する必要がある。
     * @param loadOffset 読み込みを開始するオフセット。0以上の値を指定する必要がある。
     * @param searchWord 検索ワード。空でない文字列を指定する必要がある。
     * @return 処理に成功した場合は [UseCaseResult.Success] に
     *   整形されたワード検索結果の日記リスト( [DiaryYearMonthList] )を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [WordSearchResultListLoadException] を格納して返す。
     * @throws IllegalArgumentException `numLoadItems`が1未満、`loadOffset`が負数、
     *   または`searchWord`が空の場合にスローされる。
     */
    suspend operator fun invoke(
        numLoadItems: Int,
        loadOffset: Int,
        searchWord: SearchWord
    ): UseCaseResult<DiaryYearMonthList<DiaryDayListItem.WordSearchResult>, WordSearchResultListLoadException> {
        Log.i(logTag, "${logMsg}開始 (読込件数: $numLoadItems, オフセット: $loadOffset, 検索ワード: \"$searchWord\")")

        require(numLoadItems >= 1) {
            "${logMsg}不正引数_読み込みアイテム数は1以上必須 (読込件数: $numLoadItems)"
        }
        require(loadOffset >= 0) {
            "${logMsg}不正引数_読み込みオフセットは0以上必須 (オフセット: $loadOffset)"
        }

        return try {
            val wordSearchResultList =
                diaryRepository.loadWordSearchResultList(
                    numLoadItems,
                    loadOffset,
                    searchWord
                )
            val convertedList = convertWordSearchResultList(wordSearchResultList, searchWord)
            Log.i(logTag, "${logMsg}完了 (結果リスト件数: ${convertedList.countDiaries()})")
            UseCaseResult.Success(convertedList)
        } catch (e: UnknownException) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(WordSearchResultListLoadException.Unknown(e))
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_読込エラー", e)
            UseCaseResult.Failure(
                WordSearchResultListLoadException.LoadFailure(searchWord, e)
            )
        }
    }

    /**
     * 生のワード検索結果リストを、表示用の年月でグループ化されたリストに変換する。
     *
     * @param diaryList 変換対象の生のワード検索結果リスト。
     * @param searchWord 検索に使用されたキーワード。
     * @return 年月でグループ化されたワード検索結果の日記リスト。入力リストが空の場合は空のリストを返す。
     */
    private fun convertWordSearchResultList(
        diaryList: List<RawWordSearchResultListItem>,
        searchWord: SearchWord
    ): DiaryYearMonthList<DiaryDayListItem.WordSearchResult> {
        if (diaryList.isEmpty()) return DiaryYearMonthList()

        val diaryDayList =
            DiaryDayList(
                diaryList.map { convertWordSearchResultListItem(it, searchWord) }
            )
        return diaryDayList.toDiaryYearMonthList()
    }

    /**
     * 生のワード検索結果アイテムを、表示用の [DiaryDayListItem.WordSearchResult] に変換する。
     *
     * 内部で [extractWordSearchResultTargetItem] を呼び出し、検索ワードにヒットした項目情報を取得する。
     *
     * @param item 変換対象の生のワード検索結果アイテム。
     * @param searchWord 検索に使用されたキーワード。
     * @return 表示用のワード検索結果アイテム。
     */
    private fun convertWordSearchResultListItem(
        item: RawWordSearchResultListItem,
        searchWord: SearchWord
    ): DiaryDayListItem.WordSearchResult {
        val diaryItem = extractWordSearchResultTargetItem(item, searchWord)
        return DiaryDayListItem.WordSearchResult(
            item.id,
            item.date,
            item.title,
            diaryItem[itemNumberKey] as DiaryItemNumber,
            diaryItem[itemTitleKey] as DiaryItemTitle,
            diaryItem[itemCommentKey] as DiaryItemComment,
            searchWord
        )
    }

    /**
     * 生のワード検索結果アイテムから、検索ワードに実際にヒットした項目（タイトルまたはコメント）を抽出する。
     *
     * 複数の項目（項目1～項目5）の中から、指定された検索ワードがタイトルまたはコメントに含まれる
     * 最初の項目を特定する。ヒットする項目がない(ヒット先が日記タイトル)場合は、項目1の情報を返す。
     *
     * @param item 検索対象の生のワード検索結果アイテム。
     * @param searchWord 検索するキーワード。
     * @return 抽出された項目の情報（項目番号、タイトル、コメント）を格納したマップ。
     *   キーは [itemNumberKey], [itemTitleKey], [itemCommentKey]。
     * @throws IllegalStateException 項目1～項目5に対して検索ワードのヒットがなく、
     *                               返す予定の項目1のタイトルまたはコメントが `null` の場合。
     */
    // MEMO:本来はDataSource側で処理するべき内容だが、対象日記項目のみを抽出するには複雑なロジックになる為、
    //      ドメイン側で処理する。
    private fun extractWordSearchResultTargetItem(
        item: RawWordSearchResultListItem,
        searchWord: SearchWord
    ): Map<String, Any> {
        val regex = ".*$searchWord.*"
        val itemTitles = arrayOf(
            item.item1Title,
            item.item2Title,
            item.item3Title,
            item.item4Title,
            item.item5Title,
        )
        val itemComments = arrayOf(
            item.item1Comment,
            item.item2Comment,
            item.item3Comment,
            item.item4Comment,
            item.item5Comment,
        )
        var itemNumber = 0
        var itemTitle = DiaryItemTitle.empty()
        var itemComment = DiaryItemComment.empty()
        for (i in itemTitles.indices) {
            val targetItemTitle = itemTitles[i] ?: continue
            val targetItemComment = itemComments[i] ?: continue

            if (targetItemTitle.value.matches(regex.toRegex())
                || targetItemComment.value.matches(regex.toRegex())
            ) {
                itemNumber = i + 1
                itemTitle = targetItemTitle
                itemComment = targetItemComment
                break
            }
        }

        // 検索ワードが項目タイトル、コメントに含まれていない場合、アイテムNo.1を抽出
        if (itemNumber == 0) {
            itemNumber = 1
            itemTitle =
                itemTitles[0]
                    ?: throw IllegalStateException(
                        "検索ワードにヒットせずitem1を返そうとしましたが、項目1のタイトルがnullです。"
                    )
            itemComment =
                itemComments[0]
                    ?: throw IllegalStateException(
                        "検索ワードにヒットせずitem1を返そうとしましたが、項目1のコメントがnullです。"
                    )
        }

        val result: MutableMap<String, Any> = HashMap()
        result[itemNumberKey] = DiaryItemNumber(itemNumber)
        result[itemTitleKey] = itemTitle
        result[itemCommentKey] = itemComment
        return result
    }
}
