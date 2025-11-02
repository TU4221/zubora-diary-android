package com.websarva.wings.android.zuboradiary.ui.model.event

internal class ConsumableEvent<out T: UiEvent>(private val content: T) {

    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}
