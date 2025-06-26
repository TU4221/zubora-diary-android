package com.websarva.wings.android.zuboradiary.domain.usecase.diary.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class SaveDiaryError(
    message: String,
    cause: Throwable
) : UseCaseError(message, cause) {

    class SaveDiary(
        cause: Throwable
    ) : SaveDiaryError(
        "日記保存に失敗しました。",
        cause
    )

    class ManageUriPermission(
        cause: Throwable
    ) : SaveDiaryError(
        "Uri権限管理に失敗しました。",
        cause
    )
}
