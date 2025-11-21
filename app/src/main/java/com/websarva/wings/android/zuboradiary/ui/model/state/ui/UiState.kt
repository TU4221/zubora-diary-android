package com.websarva.wings.android.zuboradiary.ui.model.state.ui

/**
 * 各画面のUI状態を表すクラスが実装すべきマーカーインターフェース。
 *
 * このインターフェースを実装するクラスは、UIの現在の状態を保持する。
 *
 * @property isProcessing 何らかの処理が進行中であるかを示す。
 * @property isInputDisabled ユーザーの入力が無効化されているかを示す。
 */
interface UiState {
    val isProcessing: Boolean
    val isInputDisabled: Boolean
}
