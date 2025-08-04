package com.websarva.wings.android.zuboradiary.domain.usecase.exception

internal sealed class DeleteAllDiariesUseCaseException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    class AllDiariesDeletionFailure(
        cause: Throwable
    ) : DeleteAllDiariesUseCaseException(
        "全日記の削除に失敗しました。",
        cause
    )

    class AllPersistableUriPermissionReleaseFailure(
        cause: Throwable
    ) : DeleteAllDiariesUseCaseException(
        "全ての永続的URI権限の解放に失敗しました。",
        cause
    )
}
