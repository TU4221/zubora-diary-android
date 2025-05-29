package com.websarva.wings.android.zuboradiary.ui.model.result

import java.io.Serializable

internal sealed class DialogResult<out T> : Serializable {

    data class Positive<out T>(
        val data: T
    ) : DialogResult<T>()

    data object Negative : DialogResult<Nothing>() {
        private fun readResolve(): Any = Negative
    }

    data object Cancel : DialogResult<Nothing>() {
        private fun readResolve(): Any = Cancel
    }
}
