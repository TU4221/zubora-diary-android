package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage

/**
 * 複数の画面(`Fragment`)で共通して使用される、UIイベントを表すsealed class。
 */
sealed class CommonUiEvent : UiEvent {
    /**
     * アプリケーションメッセージ（情報、警告、エラーなど）を表示することを示すイベント。
     * @property message 表示するメッセージのコンテンツを保持する[AppMessage]。
     */
    data class NavigateAppMessage(val message: AppMessage) : CommonUiEvent()

    /**
     * 前の画面へ遷移することを示すイベント。
     * @param resultData 遷移元の画面へ返す結果データ。返す結果が無い場合はnullを代入する。
     * @param T 遷移元へ返す結果データの型。
     */
    data class NavigatePreviousFragment<T>(val resultData: T?) : CommonUiEvent()
}
