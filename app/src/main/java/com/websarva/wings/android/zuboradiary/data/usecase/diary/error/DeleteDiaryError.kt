package com.websarva.wings.android.zuboradiary.data.usecase.diary.error

import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseError

internal sealed class DeleteDiaryError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class DeleteDiary(
        cause: Throwable? = null
    ) : DeleteDiaryError(
        "日記削除に失敗しました。",
        cause
    )

    class ReleaseUriPermission(
        cause: Throwable?
    ) : DeleteDiaryError(
        "Uri権限解放に失敗しました。",
        cause
    )
}
