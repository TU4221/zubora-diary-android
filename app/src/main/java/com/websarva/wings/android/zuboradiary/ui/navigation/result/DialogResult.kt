package com.websarva.wings.android.zuboradiary.ui.navigation.result

import java.io.Serializable

/**
 * ダイアログ操作による選択結果。
 *
 * ユーザーの操作に応じて、肯定（[Positive]）、否定（[Negative]）、キャンセル（[Cancel]）のいずれかの状態を持つ。
 *
 * @param T 肯定的な選択がなされた場合に保持される値の型。
 */
// HACK: Parcelableを実装するとジェネリクスTがParcelable制約を受けるため、
//       Unit等も扱えるようSerializableを採用している。
sealed interface DialogResult<out T> : NavigationResult, Serializable {

    /**
     * 肯定的な選択（「はい」、「OK」など）がなされた状態。
     *
     * @property data 保持されている具体的なデータ。
     */
    data class Positive<out T>(
        val data: T
    ) : DialogResult<T>

    /**
     * 否定的な選択（「いいえ」、「キャンセル」ボタンなど）がなされた状態。
     */
    data object Negative : DialogResult<Nothing> {
        // HACK: Serializable実装時にreadResolveの記述が推奨されるが、未使用のため警告を抑制
        @Suppress("unused")
        private fun readResolve(): Any = Negative
    }

    /**
     * 操作が中断（ダイアログ外タップ、戻るボタンなど）された状態。
     */
    data object Cancel : DialogResult<Nothing> {
        // HACK: Serializable実装時にreadResolveの記述が推奨されるが、未使用のため警告を抑制
        @Suppress("unused")
        private fun readResolve(): Any = Cancel
    }
}
