package com.websarva.wings.android.zuboradiary.domain.usecase.diary.image.exception

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.image.BuildDiaryImageFilePathUseCase

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
        imageFileName: DiaryImageFileName,
        cause: Throwable
    ) : DiaryImageFilePathBuildingException(
        "日記画像ファイル `$imageFileName` のパスの生成に失敗しました。",
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryImageFilePathBuildingException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
