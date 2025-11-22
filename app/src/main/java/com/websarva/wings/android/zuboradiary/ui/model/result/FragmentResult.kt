package com.websarva.wings.android.zuboradiary.ui.model.result

import java.io.Serializable

/**
 * Fragment間のナビゲーションで結果を返すために使用されるsealed class。
 *
 * このクラスは、結果が存在する場合([Some])と、存在しない場合([None])の2つの状態を表現する。
 *
 * @param T [Some]の場合に返されるデータの型。
 */
// HACK:Parcelableを実装するとデータの型(ジェネリクスの型)がParcelableのみしか受け付けなくなるため、
//      Serializableで対応。(ParcelableだとUnitが指定できなくなる)
sealed class FragmentResult<out T> : NavigationResult, Serializable {

    /**
     * 何らかのデータを持つ結果を表す。
     * @param key Fragmentから返されるデータのキー。
     * @property data Fragmentから返される具体的なデータ。
     */
    data class Some<out T>(val key: String, val data: T) : FragmentResult<T>()

    /** 結果データが存在しないことを示す。 */
    data object None : FragmentResult<Nothing>() {
        // HACK:Serializable実装すると下記を記述するように促されるが、未使用の為、下記アノテーションで警告抑制
        @Suppress("unused")
        private fun readResolve(): Any = None
    }
}
