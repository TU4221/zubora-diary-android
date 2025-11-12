package com.websarva.wings.android.zuboradiary.ui.model.navigation

import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable
import java.util.UUID

data class PendingNavigationCommand (
    val command: NavigationCommand,
    override val id: UUID = UUID.randomUUID(),
    val currentRetryCount: Int = 0,
) : Identifiable {

    init {
        require(currentRetryCount >= 0)
    }

    companion object {
        private const val MAX_RETRIES = 3 // 許容される最大リトライ回数
    }

    fun canRetry(): Boolean = currentRetryCount < MAX_RETRIES

    fun incrementRetryCount(): PendingNavigationCommand {
        return this.copy(currentRetryCount = currentRetryCount.inc())
    }
}

