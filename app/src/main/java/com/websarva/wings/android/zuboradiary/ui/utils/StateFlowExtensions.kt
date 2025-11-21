package com.websarva.wings.android.zuboradiary.ui.utils

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

// TODO:削除
/**
 * [StateFlow]の現在の`value`がnullでないことを表明し、非null型として返す拡張関数。
 * @return `value`がnullでない場合はその値。
 * @throws IllegalStateException `value`がnullの場合。
 */
internal fun <T> StateFlow<T?>.requireValue(): T {
    return checkNotNull(value)
}
// TODO:削除
/**
 * [StateFlow]から発行される最初の非null値を待機して返す、suspend拡張関数。
 * @return 待機後に受け取った最初の非null値。
 */
suspend fun <T> StateFlow<T?>.firstNotNull(): T {
    return this.filterNotNull().first()
}
