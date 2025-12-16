package com.websarva.wings.android.zuboradiary.ui.common.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 入力テキストのバリデーション結果の状態を表すUI状態モデル。
 *
 * テキスト検証の様々な結果を表現する。
 */
@Parcelize
sealed interface InputTextValidationState : Parcelable {

    /** 入力が有効であることを示す。 */
    data object Valid : InputTextValidationState

    /** 入力が不正であることを示す（汎用的なエラー）。 */
    data object Invalid : InputTextValidationState

    /** 入力が空であることを示す。 */
    data object InvalidEmpty : InputTextValidationState

    /** 入力の開始文字が不正であることを示す。 */
    data object InvalidInitialCharUnmatched : InputTextValidationState
}
