package com.websarva.wings.android.zuboradiary.ui.navigation.event.destination

import com.websarva.wings.android.zuboradiary.ui.navigation.event.NavigationEvent

/**
 * アプリケーションにおける後方遷移の目的地を表すマーカーインターフェース。
 *
 * [NavigationEvent.Back] において、戻り先を型安全に指定するために使用する。
 * 具体的な戻り先定義は、このインターフェースを実装して定義する。
 */
interface AppNavBackDestination
