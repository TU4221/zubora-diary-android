package com.websarva.wings.android.zuboradiary.ui.common.navigation.result

import java.io.Serializable

/**
 * フラグメント間のナビゲーション結果。
 *
 * 結果データが存在する場合（[Some]）と、存在しない場合（[None]）の2つの状態を持つ。
 *
 * @param T データが存在する場合に保持される値の型。
 */
// HACK: Parcelableを実装するとジェネリクスTがParcelable制約を受けるため、
//       Unit等も扱えるようSerializableを採用している。
sealed interface FragmentResult<out T> : NavigationResult, Serializable {

    /**
     * 結果データが存在する状態。
     *
     * @property data 保持されている具体的なデータ。
     */
    data class Some<out T>(val data: T) : FragmentResult<T>

    /**
     * 結果データが存在しない状態。
     */
    data object None : FragmentResult<Nothing> {
        // HACK:Serializable実装すると下記を記述するように促されるが、未使用の為、下記アノテーションで警告抑制
        @Suppress("unused")
        private fun readResolve(): Any = None
    }
}
