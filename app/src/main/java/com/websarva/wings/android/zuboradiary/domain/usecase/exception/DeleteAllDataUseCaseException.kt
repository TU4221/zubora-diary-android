package com.websarva.wings.android.zuboradiary.domain.usecase.exception

internal sealed class DeleteAllDataUseCaseException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    class AllDataDeletionFailure(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全データの削除に失敗しました。",
        cause
    )

    class AllPersistableUriPermissionReleaseFailure(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全ての永続的URI権限の解放に失敗しました。",
        cause
    )

    class AllSettingsInitializationFailure(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全ての設定の初期化に失敗しました。",
        cause
    )
}
