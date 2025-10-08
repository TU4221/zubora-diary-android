package com.websarva.wings.android.zuboradiary.ui.model.result

import java.io.Serializable

// MEMO:Parcelableを実装するとポジティブデータの型(ジェネリクスの型)がParcelableのみしか受け付けなくなるため、
//      Serializableで対応。
internal sealed class DialogResult<out T> : NavigationResult, Serializable {

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
