package com.websarva.wings.android.zuboradiary.ui.model.event

/**
 * ActivityからFragmentへ通知される、UIイベント。
 *
 * このクラスは、BottomNavigationViewの同じタブが再選択された場合など、
 * Activityレベルで発生し、Fragmentで特定のUIアクションをトリガーする必要があるイベントを定義する。
 */
sealed interface ActivityCallbackUiEvent : UiEvent {
    
    /** BottomNavigationViewの選択中アイテムが再選択されたことを示すイベント。 */
    data object ProcessOnBottomNavigationItemReselect : ActivityCallbackUiEvent
}
