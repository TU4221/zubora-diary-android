package com.websarva.wings.android.zuboradiary.domain.usecase.diary.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class DeleteDiaryError(
    message: String,
    cause: Throwable
) : UseCaseError(message, cause) {

    class DeleteDiary(
        cause: Throwable
    ) : DeleteDiaryError(
        "日記削除に失敗しました。",
        cause
    )

    class ReleaseUriPermission(
        cause: Throwable
    ) : DeleteDiaryError(
        "Uri権限解放に失敗しました。",
        cause
    )
}
