package com.websarva.wings.android.zuboradiary.ui.common.viewmodel

import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.common.state.UiState
import com.websarva.wings.android.zuboradiary.ui.common.fragment.OnBackPressedHandler
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.AppNavDestination

/**
 * Fragment向けに特化したViewModelの基底クラス。
 *
 * [BaseViewModel] の機能に加え、システムバックボタン押下時の処理（[OnBackPressedHandler]）を実装する責務を持つ。
 * 画面遷移イベント [NavigationEvent] における前方・後方の遷移先型を定義する。
 *
 * @param US このViewModelが管理するUI状態の型。
 * @param UE このViewModelが発行するUIイベントの型。
 * @param ND 前方遷移先の型。
 * @param NBD 後方遷移先の型。
 */
abstract class BaseFragmentViewModel<
        US: UiState,
        UE: UiEvent,
        ND: AppNavDestination,
        NBD: AppNavBackDestination
> internal constructor(
    initialViewUiState: US
) : BaseViewModel<US, UE, NavigationEvent<ND, NBD>>(initialViewUiState), OnBackPressedHandler
