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

    class RevokeAllPersistentAccessUriFailed(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全てのUriの永続的なアクセス権の取り消しに失敗しました。",
        cause
    )
}
