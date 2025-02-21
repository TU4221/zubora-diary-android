package com.websarva.wings.android.zuboradiary.ui

import kotlinx.coroutines.flow.StateFlow

fun <T> StateFlow<T?>.checkNotNull(): T {
    return checkNotNull(value)
}
