package com.websarva.wings.android.zuboradiary.ui.model.result

import java.io.Serializable

/**
 * ダイアログからの結果を表現するためのsealed class。
 *
 * このクラスは、ユーザーのダイアログ操作（肯定、否定、キャンセル）に応じて、
 * 異なる状態とデータをカプセル化する。
 *
 * @param T [Positive]の場合に返されるデータの型。
 */
// HACK:Parcelableを実装するとポジティブデータの型(ジェネリクスの型)がParcelableのみしか受け付けなくなるため、
//      Serializableで対応。(ParcelableだとUnitが指定できなくなる)
sealed class DialogResult<out T> : NavigationResult, Serializable {

    /**
     * 肯定的な選択（例：「はい」、「OK」）が行われたことを示す。
     * @property data ダイアログから返される具体的なデータ。
     */
    data class Positive<out T>(
        val data: T
    ) : DialogResult<T>()

    /** 否定的な選択（例：「いいえ」、「キャンセル」）が行われたことを示す。 */
    data object Negative : DialogResult<Nothing>() {
        // HACK:Serializable実装すると下記を記述するように促されるが、未使用の為、下記アノテーションで警告抑制
        @Suppress("unused")
        private fun readResolve(): Any = Negative
    }

    /** ダイアログがキャンセル（例：ダイアログ外のタップ、戻るボタン）されたことを示す。 */
    data object Cancel : DialogResult<Nothing>() {
        // HACK:Serializable実装すると下記を記述するように促されるが、未使用の為、下記アノテーションで警告抑制
        @Suppress("unused")
        private fun readResolve(): Any = Cancel
    }
}
