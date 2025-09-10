package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.ImageUriUsageCheckException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 指定された画像Uriがのいずれかの日記データで使用されているかどうかを確認するユースケース。
 *
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
internal class IsImageUriUsedInDiariesUseCase(
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "画像Uri使用確認_"

    /**
     * ユースケースを実行し、指定された画像Uriがのいずれかの日記データで使用されているかどうかを返す。
     *
     * @param uriString 確認対象の画像URI文字列。
     * @return 画像Uriが使用されている場合は [UseCaseResult.Success] に `true` を、
     *         使用されていない場合は `false` を格納して返す。
     *   使用確認処理に失敗した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        uriString: String
    ): UseCaseResult<Boolean, ImageUriUsageCheckException> {
        Log.i(logTag, "${logMsg}開始 (ImageURI: $uriString)")
        return try {
            val exists = diaryRepository.existsImageUri(uriString)
            Log.i(logTag, "${logMsg}完了 (結果: $exists)")
            UseCaseResult.Success(exists)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_使用確認処理エラー", e)
            UseCaseResult.Failure(
                ImageUriUsageCheckException.CheckFailure(uriString, e)
            )
        }
    }
}
