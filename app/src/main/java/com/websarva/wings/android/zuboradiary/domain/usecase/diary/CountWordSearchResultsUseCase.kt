package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.WordSearchResultCountFailureException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 指定された検索ワードに一致する日記の総数を取得するユースケース。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class CountWordSearchResultsUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "ワード検索結果日記総数取得_"

    /**
     * ユースケースを実行し、指定された検索ワードに一致する日記の総数を返す。
     *
     * @param searchWord 検索するキーワード。
     * @return 検索ワードに一致した日記の総数を [UseCaseResult.Success] に格納して返す。
     *   日記数のカウントに失敗した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        searchWord: String
    ): UseCaseResult<Int, WordSearchResultCountFailureException> {
        Log.i(logTag, "${logMsg}開始 (検索ワード: \"$searchWord\")")

        return try {
            val numDiaries =diaryRepository.countWordSearchResults(searchWord)
            Log.i(logTag, "${logMsg}完了 (結果: $numDiaries)")
            UseCaseResult.Success(numDiaries)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_カウント処理エラー", e)
            UseCaseResult.Failure(
                WordSearchResultCountFailureException.CountFailure(searchWord, e)
            )
        }
    }
}
