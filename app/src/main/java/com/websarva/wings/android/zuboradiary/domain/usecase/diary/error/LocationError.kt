package com.websarva.wings.android.zuboradiary.domain.usecase.diary.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class LocationError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class AccessLocation(
        cause: Throwable? = null
    ) : LocationError(
        "位置情報取得に失敗しました。",
        cause
    )
}
