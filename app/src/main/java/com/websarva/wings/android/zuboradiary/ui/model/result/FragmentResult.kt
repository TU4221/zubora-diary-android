package com.websarva.wings.android.zuboradiary.ui.model.result

import java.io.Serializable

internal sealed class FragmentResult<out T> : Serializable {

    data class Some<out T>(
        val data: T
    ) : FragmentResult<T>()

    data object None : FragmentResult<Nothing>() {
        private fun readResolve(): Any = None
    }
}
