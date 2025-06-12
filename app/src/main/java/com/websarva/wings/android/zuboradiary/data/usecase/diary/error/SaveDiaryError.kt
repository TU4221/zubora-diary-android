package com.websarva.wings.android.zuboradiary.data.usecase.diary.error

import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseError

internal sealed class SaveDiaryError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class SaveDiary(
        cause: Throwable? = null
    ) : SaveDiaryError(
        "日記保存に失敗しました。",
        cause
    )

    class ManageUriPermission(
        cause: Throwable?
    ) : SaveDiaryError(
        "Uri権限管理に失敗しました。",
        cause
    )
}
