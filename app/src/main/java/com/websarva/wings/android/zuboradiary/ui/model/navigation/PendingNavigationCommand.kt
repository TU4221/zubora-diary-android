package com.websarva.wings.android.zuboradiary.ui.model.navigation

import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable
import java.util.UUID

/**
 * 実行に失敗し、再試行を待っている保留中のナビゲーションコマンドを表すsealed class。
 *
 * 画面遷移が何らかの理由で即座に実行できなかった場合に、元の[NavigationCommand]をラップし、
 * リトライ回数を管理する責務を持つ。
 * [createFrom]ファクトリメソッドを通してのみインスタンスを生成させることで、
 * リトライ回数の初期値が常に0であることを保証する。
 *
 * @property command 保留されている元のナビゲーションコマンド。
 * @property id この保留コマンドの一意な識別子。
 * @property currentRetryCount 現在のリトライ回数。
 */
@ConsistentCopyVisibility
data class PendingNavigationCommand private constructor(
    val command: NavigationCommand,
    override val id: UUID,
    val currentRetryCount: Int,
) : Identifiable {

    init {
        require(currentRetryCount >= 0)
    }

    /**
     * このコマンドがまだ再試行可能かどうかを判定する。
     * @return 最大リトライ回数に達していない場合は`true`。
     */
    fun canRetry(): Boolean = currentRetryCount < MAX_RETRIES

    /**
     * リトライ回数を1つインクリメントした新しい[PendingNavigationCommand]インスタンスを返す。
     * @return リトライ回数が更新された新しいインスタンス。
     */
    fun incrementRetryCount(): PendingNavigationCommand {
        return this.copy(currentRetryCount = currentRetryCount.inc())
    }

    companion object {
        /** 許容される最大リトライ回数 */
        private const val MAX_RETRIES = 3

        /**
         * ナビゲーションコマンドから、初期状態の[PendingNavigationCommand]を生成する。
         *
         * @param command 保留する元のナビゲーションコマンド。
         * @return リトライ回数が0に設定された、新しいインスタンス。
         */
        fun createFrom(command: NavigationCommand): PendingNavigationCommand {
            return PendingNavigationCommand(
                command = command,
                id = UUID.randomUUID(),
                currentRetryCount = 0
            )
        }
    }
}
