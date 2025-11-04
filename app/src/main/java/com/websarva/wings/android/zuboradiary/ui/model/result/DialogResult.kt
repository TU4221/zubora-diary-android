package com.websarva.wings.android.zuboradiary.ui.model.result

import java.io.Serializable

// HACK:Parcelableを実装するとポジティブデータの型(ジェネリクスの型)がParcelableのみしか受け付けなくなるため、
//      Serializableで対応。(ParcelableだとUnitが指定できなくなる)
sealed class DialogResult<out T> : NavigationResult, Serializable {

    data class Positive<out T>(
        val data: T
    ) : DialogResult<T>()

    data object Negative : DialogResult<Nothing>() {
        // HACK:Serializable実装すると下記を記述するように促されるが、未使用の為、下記アノテーションで警告抑制
        @Suppress("unused")
        private fun readResolve(): Any = Negative
    }

    data object Cancel : DialogResult<Nothing>() {
        // HACK:Serializable実装すると下記を記述するように促されるが、未使用の為、下記アノテーションで警告抑制
        @Suppress("unused")
        private fun readResolve(): Any = Cancel
    }
}
