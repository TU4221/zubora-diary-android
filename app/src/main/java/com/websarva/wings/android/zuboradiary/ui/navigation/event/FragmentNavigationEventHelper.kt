package com.websarva.wings.android.zuboradiary.ui.navigation.event

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.websarva.wings.android.zuboradiary.MobileNavigationDirections
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.AppNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.AppNavDestination
import com.websarva.wings.android.zuboradiary.ui.navigation.result.FragmentResult

/**
 * Fragmentにおける画面遷移処理を補助するヘルパークラス。
 *
 * 主に以下の責務を持つ:
 * - ライフサイクル状態を確認した上での安全な画面遷移（[NavController] 操作）の実行
 * - 戻り遷移（Back）時における、[SavedStateHandle] を介した前画面への結果データの受け渡し
 * - アプリケーションメッセージダイアログへの[NavDirections]の生成
 */
class FragmentNavigationEventHelper {

    /**
     * 遷移操作が可能かを検証し、実行する。
     *
     * ライフサイクルが有効かつ現在地が正しい場合のみ、[NavigationEvent] に基づき [NavController] を操作する。
     * 戻り遷移時に [event] が結果データを持つ場合、バックスタックエントリを通じてデータを設定する。
     *
     * @param viewLifecycle 遷移元のViewライフサイクル。
     * @param navController 操作対象のNavController。
     * @param event 処理対象の遷移イベント。
     * @param handler 遷移先を提供するハンドラ。
     * @param callback 成功・失敗時のコールバックプロバイダ。
     * @return 遷移処理が実行された場合は true、条件不一致で中断した場合は false。
     */
    fun <ND : AppNavDestination, NBD : AppNavBackDestination> executeFragmentNavigation(
        viewLifecycle: Lifecycle,
        navController: NavController,
        event: NavigationEvent<ND, NBD>,
        handler: FragmentNavigationEventHandler<ND, NBD>,
        callback: NavigationEventCallback<NavigationEvent<ND, NBD>>
    ): Boolean {
        if (!canNavigateFragment(viewLifecycle, navController, handler.destinationId)) {
            Log.d(logTag, "画面遷移不可_$event")
            callback.onNavigationEventFailure(event)
            return false
        }

        Log.d(logTag, "画面遷移ナビゲーション起動_$event")
        when (event) {
            is NavigationEvent.To<ND> -> {
                navController.navigate(
                    handler.toNavDirections(event.destination)
                )
            }
            is NavigationEvent.Back<NBD, *> -> {
                if (event.destination == null) {
                    if (event.resultData != null) {
                        val resultKey = checkNotNull(handler.resultKey)
                        val previousBackStackEntry = checkNotNull(navController.previousBackStackEntry)
                        previousBackStackEntry.savedStateHandle[resultKey] =
                            FragmentResult.Some(event.resultData)
                    }
                    navController.popBackStack()
                } else {
                    val navDestinationId = handler.toNavDestinationId(event.destination)
                    if (event.resultData != null) {
                        val resultKey = checkNotNull(handler.resultKey)
                        val previousBackStackEntry = navController.getBackStackEntry(navDestinationId)
                        previousBackStackEntry.savedStateHandle[resultKey] =
                            FragmentResult.Some(event.resultData)
                    }
                    navController.popBackStack(navDestinationId, false)
                }
            }
        }
        callback.onNavigationEventSuccess(event)
        return true
    }

    /**
     * 遷移操作が可能か判定する。
     *
     * Viewが [Lifecycle.State.RESUMED] 状態であり、
     * かつ [NavController] の現在地が期待地と一致することを確認する。
     *
     * @param viewLifecycle 検証するライフサイクル。
     * @param navController 検証するNavController。
     * @param fragmentDestinationId 現在期待されるデスティネーションID。
     * @return 遷移可能な場合は true。
     */
    private fun canNavigateFragment(
        viewLifecycle: Lifecycle,
        navController: NavController,
        fragmentDestinationId: Int
    ): Boolean {
        val lifecycleState = viewLifecycle.currentState
        if (lifecycleState != Lifecycle.State.RESUMED) return false

        val currentDestination = navController.currentDestination ?: return false
        return currentDestination.id == fragmentDestinationId
    }

    /**
     * アプリケーションメッセージダイアログへ遷移する為の [NavDirections] オブジェクトを生成する。
     *
     * @param appMessage 表示するメッセージデータ。
     * @return グローバルアクションとして定義された [NavDirections]。
     */
    fun createAppMessageDialogNavDirections(appMessage: AppMessage): NavDirections {
        return MobileNavigationDirections.actionGlobalToAppMessageDialog(appMessage)
    }
}
