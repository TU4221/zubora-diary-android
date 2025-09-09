package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.UnloadedWordSearchResultsExistCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 指定された検索ワードに一致する日記のうち、まだ読み込まれていないものが存在するかどうかを確認するユースケース。
 *
 * このユースケースは、特定の検索ワードに合致する日記の総数と、
 * 現在UI上に読み込まれている該当の日記の数を比較し、未読込の日記が存在するかを判定する。
 * 検索結果の追加データをロードする必要があるかどうかを判断するために使用される。
 *
 * @property countWordSearchResultsUseCase 検索ワードに一致する日記の総数を取得するためのユースケース。
 */
internal class CheckUnloadedWordSearchResultsExistUseCase(
    private val countWordSearchResultsUseCase: CountWordSearchResultsUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "未読込ワード検索結果日記確認_"

    /**
     * ユースケースを実行し、指定された検索ワードに一致する未読込の日記が存在するかどうかを返す。
     *
     * @param searchWord 検索するキーワード。
     * @param numLoadedDiaries 現在UIなどに読み込まれている、この検索ワードに一致する日記の数。
     * @return 未読込の検索結果の日記が存在する場合は [UseCaseResult.Success] に `true` を、
     *         存在しない場合や検索結果が0件の場合は `false` を格納して返す。
     *   検索結果の日記数のカウントに失敗した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        searchWord: String,
        numLoadedDiaries: Int
    ): UseCaseResult<Boolean, UnloadedWordSearchResultsExistCheckException> {
        Log.i(logTag, "${logMsg}開始 (検索ワード: \"$searchWord\", 読込済件数: $numLoadedDiaries)")

        return when (val result = countWordSearchResultsUseCase(searchWord)) {
            is UseCaseResult.Success -> {
                val numExistingDiaries = result.value
                val unloadedDiariesExist =
                    if (numExistingDiaries <= 0) {
                        false
                    } else {
                        numLoadedDiaries < numExistingDiaries
                    }
                Log.i(logTag, "${logMsg}完了 (結果: $unloadedDiariesExist)")
                UseCaseResult.Success(unloadedDiariesExist)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗_検索結果カウントエラー", result.exception)
                UseCaseResult.Failure(
                    UnloadedWordSearchResultsExistCheckException.CheckFailure(result.exception)
                )
            }
        }
    }
}
