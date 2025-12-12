package com.websarva.wings.android.zuboradiary.ui.navigation.event

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator
import com.websarva.wings.android.zuboradiary.MobileNavigationDirections
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.AppNavDestination

/**
 * Activityにおける画面遷移処理を補助するヘルパークラス。
 *
 * 主に以下の責務を持つ:
 * - ライフサイクル状態を確認した上での安全な画面遷移（[NavController] 操作）の実行
 * - アプリケーションメッセージダイアログへの[NavDirections]の生成
 */
class ActivityNavigationEventHelper {

    /**
     * 遷移操作が可能かを検証し、実行する。
     *
     * ライフサイクルが有効かつ現在地が適切な場合のみ、[NavigationEvent.To] に基づき [NavController] を操作する。
     *
     * @param activityLifecycle 遷移元のActivityライフサイクル。
     * @param navController 操作対象のNavController。
     * @param event 処理対象の遷移イベント。
     * @param handler 遷移先を提供するハンドラ。
     * @param callback 成功・失敗時のコールバックプロバイダ。
     * @return 遷移処理が実行された場合は true、条件不一致で中断した場合は false。
     */
    fun <ND : AppNavDestination> executeFragmentNavigation(
        activityLifecycle: Lifecycle,
        navController: NavController,
        event: NavigationEvent.To<ND>,
        handler: ActivityNavigationEventHandler<ND>,
        callback: NavigationEventCallback<NavigationEvent.To<ND>>
    ): Boolean {
        if (!canNavigateFragment(activityLifecycle, navController)) {
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
        }
        callback.onNavigationEventSuccess(event)
        return true
    }

    /**
     * 遷移操作が可能か判定する。
     *
     * Activityが [Lifecycle.State.RESUMED] 状態であり、
     * かつ [NavController] の現在地がFragmentであることを確認する。
     *
     * @param activityLifecycle 検証するライフサイクル。
     * @param navController 検証するNavController。
     * @return 遷移可能な場合は true。
     */
    private fun canNavigateFragment(
        activityLifecycle: Lifecycle,
        navController: NavController
    ): Boolean {
        if (activityLifecycle.currentState != Lifecycle.State.RESUMED) return false

        val currentDestination = navController.currentDestination ?: return false
        return currentDestination is FragmentNavigator.Destination
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
    //endregion
}
