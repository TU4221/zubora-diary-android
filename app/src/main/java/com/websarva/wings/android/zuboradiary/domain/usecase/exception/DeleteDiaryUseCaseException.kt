package com.websarva.wings.android.zuboradiary.domain.usecase.exception

internal sealed class DeleteDiaryUseCaseException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    class DeleteDiaryFailed(
        cause: Throwable
    ) : DeleteDiaryUseCaseException(
        "日記の削除に失敗しました。",
        cause
    )

    class RevokePersistentAccessUriFailed(
        cause: Throwable
    ) : DeleteDiaryUseCaseException(
        "Uriの永続的なアクセス権の取り消しに失敗しました。",
        cause
    )
}
