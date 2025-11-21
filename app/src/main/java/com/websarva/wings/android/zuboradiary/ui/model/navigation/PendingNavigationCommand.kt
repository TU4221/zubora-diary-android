package com.websarva.wings.android.zuboradiary.ui.model.navigation

import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable
import java.util.UUID

// TODO:生成メソッド用意？
/**
 * 実行に失敗し、再試行を待っている保留中のナビゲーションコマンドを表すsealed class。
 *
 * 画面遷移が何らかの理由で即座に実行できなかった場合に、元の[NavigationCommand]をラップし、
 * リトライ回数を管理する責務を持つ。
 *
 * @property command 保留されている元のナビゲーションコマンド。
 * @property id この保留コマンドの一意な識別子。
 * @property currentRetryCount 現在のリトライ回数。
 */
data class PendingNavigationCommand (
    val command: NavigationCommand,
    override val id: UUID = UUID.randomUUID(),
    val currentRetryCount: Int = 0,
) : Identifiable {

    init {
        require(currentRetryCount >= 0)
    }

    companion object {
        /** 許容される最大リトライ回数 */
        private const val MAX_RETRIES = 3
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
}
