package com.websarva.wings.android.zuboradiary.domain.usecase.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase

/**
 * [DeleteAllDataUseCase]の実行中に発生しうる、より具体的な失敗原因を示す例外の基底クラス。
 *
 * このクラスの各サブクラスは、全データ削除処理における異なる失敗シナリオを表します。
 * これにより、ユースケースの呼び出し元は、発生した例外の種類に応じて、
 * より詳細なエラーハンドリングやユーザーへのフィードバックを行うことが可能になります。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal sealed class DeleteAllDataUseCaseException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 全てのデータの削除に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class AllDataDeleteFailure(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全データの削除に失敗しました。",
        cause
    )

    /**
     * 全ての永続的URI権限の解放に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class AllPersistableUriPermissionReleaseFailure(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全ての永続的URI権限の解放に失敗しました。",
        cause
    )

    /**
     * 全ての設定の初期化に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class AllSettingsInitializationFailure(
        cause: Throwable
    ) : DeleteAllDataUseCaseException(
        "全ての設定の初期化に失敗しました。",
        cause
    )
}
