package com.websarva.wings.android.zuboradiary.ui.utils

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

internal fun <T> StateFlow<T?>.requireValue(): T {
    return checkNotNull(value)
}

suspend fun <T> StateFlow<T?>.firstNotNull(): T {
    return this.filterNotNull().first()
}
