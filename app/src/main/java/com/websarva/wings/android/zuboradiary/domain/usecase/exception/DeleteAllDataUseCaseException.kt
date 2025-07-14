package com.websarva.wings.android.zuboradiary.domain.usecase.exception

internal sealed class DeleteAllDataUseCaseException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    class DeleteAllDataFailed(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全データの削除に失敗しました。",
        cause
    )

    class ReleaseAllPersistableUriPermissionFailed(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全ての永続的URI権限の解放に失敗しました。",
        cause
    )
}
