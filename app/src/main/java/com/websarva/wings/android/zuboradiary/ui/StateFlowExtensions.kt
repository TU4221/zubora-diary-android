package com.websarva.wings.android.zuboradiary.ui

import kotlinx.coroutines.flow.StateFlow

internal fun <T> StateFlow<T?>.requireValue(): T {
    return checkNotNull(value)
}
