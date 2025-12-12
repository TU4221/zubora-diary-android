package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.NavigationResult
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.theme.withTheme
import com.websarva.wings.android.zuboradiary.ui.viewmodel.MainActivityViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Fragmentで利用される共通処理をまとめたヘルパークラス。
 *
 * 以下の責務を持つ:
 * - テーマカラーを適用したView Inflaterの生成
 * - フラグメント、ダイアログからの結果受け取り機能のセットアップ
 * - ViewModelからの各種UIイベントの監視セットアップ
 * - Navigation Componentを用いた画面遷移の実行とリトライ処理
 * - バックプレス処理の移譲
 * - ライフサイクルを考慮したコルーチンの起動
 */
class FragmentHelper {

    //region View Inflater Helpers
    /**
     * 指定されたテーマカラーを適用した新しいLayoutInflaterを生成する。
     * @param inflater 元となるLayoutInflater
     * @param themeColor 適用するテーマカラー
     * @return テーマが適用された新しいLayoutInflaterインスタンス
     */
    fun createThemeColorInflater(
        inflater: LayoutInflater,
        themeColor: ThemeColorUi
    ): LayoutInflater {
        return inflater.withTheme(themeColor)
    }
    //endregion

    //region UI Observation Helpers
    /**
     * Fragment Result APIを安全に監視し、結果を受け取る。
     * @param navController 画面遷移を管理するNavController
     * @param fragmentDestinationId 結果を監視するフラグメントのDestination ID
     * @param key 結果を識別するための一意なキー
     * @param onResultReceived 結果を受け取った際の処理を行うコールバック
     */
    fun <R : NavigationResult> observeFragmentResult(
        navController: NavController,
        fragmentDestinationId: Int,
        key: String,
        onResultReceived: (R) -> Unit
    ) {
        val navBackStackEntry = navController.getBackStackEntry(fragmentDestinationId)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        val result = savedStateHandle.getStateFlow(key, null)

        // MEMO:対象デスティネーションがRESUMEDになった際に、他の画面からの結果を安全に受け取り処理するため、
        //      NavBackStackEntryのライフサイクルを使用。これにより、結果処理後の画面遷移も適切なタイミングで行える。
        navBackStackEntry.lifecycleScope.launch {
            navBackStackEntry.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                result.filterNotNull().collect { value: R ->
                    onResultReceived(value)

                    savedStateHandle[key] = null
                }
            }
        }
    }

    /**
     * ViewModelからの画面固有UIイベントを監視する。
     * @param fragment ライフサイクルスコープのオーナーとなるFragment
     * @param mainViewModel イベントを供給するViewModel
     * @param handler イベントを処理するハンドラ
     */
    fun <E: UiEvent> observeMainUiEvent(
        fragment: Fragment,
        mainViewModel: BaseFragmentViewModel<*, E, *, *>,
        handler: MainUiEventHandler<E>
    ) {
        launchAndRepeatOnViewLifeCycleStarted(fragment) {
            mainViewModel.uiEvent
                .collect { value: ConsumableEvent<E> ->
                    val event = value.getContentIfNotHandled().also {
                        Log.d(logTag, "UiEvent_Collect(): $it")
                    } ?: return@collect

                    handler.onMainUiEventReceived(event)
                }
        }
    }

    /**
     * ActivityからのコールバックUIイベントを監視する。
     * @param fragment ライフサイクルスコープのオーナーとなるFragment
     * @param mainActivityViewModel イベントを供給するViewModel
     * @param handler イベントを処理するハンドラ
     */
    fun observeActivityUiEvent(
        fragment: Fragment,
        mainActivityViewModel: MainActivityViewModel,
        handler: ActivityCallbackUiEventHandler
    ) {
        launchAndRepeatOnViewLifeCycleStarted(fragment) {
            mainActivityViewModel.activityCallbackUiEvent
                .collect { value: ConsumableEvent<ActivityCallbackUiEvent> ->
                    val event = value.getContentIfNotHandled().also {
                        Log.d(logTag, "ActivityUiEvent_Collect(): $it")
                    } ?: return@collect

                    handler.onActivityCallbackUiEventReceived(event)
                }
        }
    }
    //endregion

    //region Back Press Helpers
    /**
     * バックプレスイベントをViewModelに委譲するためのコールバックを登録する。
     * @param fragment ライフサイクルスコープのオーナーとなるFragment
     * @param handler バックプレスイベントを処理するハンドラ
     */
    fun registerOnBackPressedCallback(
        fragment: Fragment,
        handler: OnBackPressedHandler
    ) {
        fragment.requireActivity().onBackPressedDispatcher
            .addCallback(
                fragment.viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (fragment.viewLifecycleOwner
                                .lifecycle.currentState != Lifecycle.State.RESUMED) return

                        handler.onBackPressed()
                    }
                }
            )
    }
    //endregion

    //region Coroutine Helpers
    /**
     * FragmentのViewライフサイクルがSTARTED状態の時にコルーチンを起動し、リピート実行する。
     * @param fragment ライフサイクルスコープのオーナーとなるFragment
     * @param block 実行する中断可能な処理ブロック
     */
    fun launchAndRepeatOnViewLifeCycleStarted(
        fragment: Fragment,
        block: suspend CoroutineScope.() -> Unit
    ) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }
    //endregion
}
