package com.websarva.wings.android.zuboradiary.data.usecase.diary.error

import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseError

internal sealed class LoadDiaryError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class LoadDiary(
        cause: Throwable? = null
    ) : LoadDiaryError(
        "日記読込に失敗しました。",
        cause
    )
}
