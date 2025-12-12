package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.ui.fragment.common.OnBackPressedHandler
import com.websarva.wings.android.zuboradiary.ui.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.AppNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.AppNavDestination

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
