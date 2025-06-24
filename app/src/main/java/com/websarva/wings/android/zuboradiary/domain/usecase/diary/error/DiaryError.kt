package com.websarva.wings.android.zuboradiary.domain.usecase.diary.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class DiaryError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class CountDiaries(
        cause: Throwable? = null
    ) : DiaryError(
        "日記カウントに失敗しました。",
        cause
    )

    class CheckDiaryExistence(
        cause: Throwable? = null
    ) : DiaryError(
        "日記既存確認に失敗しました。",
        cause
    )

    class CheckPicturePathUsage(
        cause: Throwable?
    ) : DiaryError(
        "Uri使用確認に失敗しました。",
        cause
    )

    class LoadDiary(
        cause: Throwable? = null
    ) : DiaryError(
        "日記読込に失敗しました。",
        cause
    )

    class SaveDiary(
        cause: Throwable? = null
    ) : DiaryError(
        "日記保存に失敗しました。",
        cause
    )

    class DeleteAndSaveDiary(
        cause: Throwable? = null
    ) : DiaryError(
        "日記削除&保存に失敗しました。",
        cause
    )

    class DeleteDiary(
        cause: Throwable? = null
    ) : DiaryError(
        "日記削除に失敗しました。",
        cause
    )

    class LoadDiaryList(
        cause: Throwable? = null
    ) : DiaryError(
        "日記リスト読込に失敗しました。",
        cause
    )

    class DeleteAllDiaries(
        cause: Throwable? = null
    ) : DiaryError(
        "全日記削除に失敗しました。",
        cause
    )

    class DeleteAllData(
        cause: Throwable? = null
    ) : DiaryError(
        "全データ削除に失敗しました。",
        cause
    )
}
