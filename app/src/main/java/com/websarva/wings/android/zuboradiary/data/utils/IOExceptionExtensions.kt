package com.websarva.wings.android.zuboradiary.data.utils

import android.system.ErrnoException
import android.system.OsConstants
import java.io.IOException

/**
 * この [IOException] がストレージ容量不足に起因するかどうかを判定。
 *
 * 以下の条件のいずれかを満たす場合に `true` を返す。
 * 1. 例外の根本原因のいずれかが [ErrnoException] であり、
 *   そのエラーコード ([ErrnoException.errno]) が [OsConstants.ENOSPC]である場合。
 * 2. 上記に該当しない場合でも、この [IOException] のメッセージに
 *    "No space left on device" (大文字・小文字を区別しない) が含まれる場合。
 *
 * @return ストレージ容量不足が原因であると判断される場合は `true`、そうでない場合は `false`。
 */
internal fun IOException.isInsufficientStorage(): Boolean {
    // 1. 原因チェーンをたどって ErrnoException と ENOSPC を確認
    var currentCause: Throwable? = this
    while (currentCause != null) {
        if (currentCause is ErrnoException && currentCause.errno == OsConstants.ENOSPC) {
            return true
        }
        currentCause = currentCause.cause
    }

    // 2. メッセージに "No space left on device" が含まれるか確認 (フォールバック)
    return this.message?.contains("No space left on device", ignoreCase = true) == true
}
