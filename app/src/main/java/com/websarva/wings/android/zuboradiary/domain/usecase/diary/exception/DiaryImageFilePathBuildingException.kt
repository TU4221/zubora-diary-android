package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase

/**
 * [BuildDiaryImageFilePathUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]、または `null`。
 */
internal sealed class DiaryImageFilePathBuildingException(
    message: String,
    cause: Throwable? = null
) : UseCaseException(message, cause) {

    /**
     * 画像ファイルのパスの取得に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class BuildingFailure(
        cause: Throwable
    ) : DiaryImageFilePathBuildingException(
        "日記画像ファイルのパスの生成に失敗しました。",
        cause
    )

    /**
     * 対象の画像ファイルが見つからなかった場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class FileNotFound(
        imageFileName: ImageFileName,
        cause: Throwable? = null
    ) : DiaryImageFilePathBuildingException(
        "日記画像ファイル `$imageFileName` が見つかりませんでした。",
        cause
    )
}
