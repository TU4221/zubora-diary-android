package com.websarva.wings.android.zuboradiary.utils

internal fun Any.createLogTag(): String {
    return this.javaClass.simpleName
}
