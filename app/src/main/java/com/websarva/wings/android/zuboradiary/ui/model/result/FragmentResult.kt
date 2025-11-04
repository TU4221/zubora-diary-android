package com.websarva.wings.android.zuboradiary.ui.model.result

import java.io.Serializable

// MEMO:Parcelableを実装するとポジティブデータの型(ジェネリクスの型)がParcelableのみしか受け付けなくなるため、
//      Serializableで対応。
sealed class FragmentResult<out T> : NavigationResult, Serializable {

    data class Some<out T>(
        val data: T
    ) : FragmentResult<T>()

    data object None : FragmentResult<Nothing>() {
        private fun readResolve(): Any = None
    }
}
