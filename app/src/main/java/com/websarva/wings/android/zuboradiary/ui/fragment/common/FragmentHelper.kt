package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.result.NavigationResult
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.ui.theme.withTheme
import com.websarva.wings.android.zuboradiary.ui.viewmodel.MainActivityViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Fragmentで利用される共通処理をまとめたヘルパークラス。
 *
 * 以下の責務を持つ:
 * - テーマカラーを適用したView Inflaterの生成
 * - Fragment Result APIの監視セットアップ
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
        mainViewModel: BaseFragmentViewModel<out UiState, E, out AppMessage>,
        handler: MainUiEventHandler<E>
    ) {
        launchAndRepeatOnViewLifeCycleStarted(fragment) {
            mainViewModel.uiEvent
                .collect { value: ConsumableEvent<E> ->
                    val event = value.getContentIfNotHandled()
                    Log.d(logTag, "UiEvent_Collect(): $event")
                    event ?: return@collect

                    handler.onMainUiEventReceived(event)
                }
        }
    }

    /**
     * ViewModelからの共通UIイベントを監視する。
     * @param fragment ライフサイクルスコープのオーナーとなるFragment
     * @param mainViewModel イベントを供給するViewModel
     * @param handler イベントを処理するハンドラ
     */
    fun <E: UiEvent> observeCommonUiEvent(
        fragment: Fragment,
        mainViewModel: BaseFragmentViewModel<out UiState, E, out AppMessage>,
        handler: CommonUiEventHandler
    ) {
        launchAndRepeatOnViewLifeCycleStarted(fragment) {
            mainViewModel.commonUiEvent
                .collect { value: ConsumableEvent<CommonUiEvent> ->
                    val event = value.getContentIfNotHandled()
                    Log.d(logTag, "Common_UiEvent_Collect(): $event")
                    event ?: return@collect

                    when (event) {
                        is CommonUiEvent.NavigatePreviousFragment ->
                            handler.navigatePreviousFragment()
                        is CommonUiEvent.NavigateAppMessage ->
                            handler.navigateAppMessageDialog(event.message)
                    }
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
                    val event = value.getContentIfNotHandled()
                    Log.d(logTag, "ActivityUiEvent_Collect(): $event")
                    event ?: return@collect

                    handler.onActivityCallbackUiEventReceived(event)
                }
        }
    }

    /**
     * 保留中の画面遷移コマンドを監視し、実行する。
     * @param navController 画面遷移を管理するNavController
     * @param navDestinationId 現在のフラグメントのDestination ID
     * @param mainViewModel 保留コマンドを保持するViewModel
     */
    fun observePendingNavigation(
        navController: NavController,
        navDestinationId: Int,
        mainViewModel: BaseFragmentViewModel<out UiState, out UiEvent, out AppMessage>
    ) {
        val navBackStackEntry = navController.getBackStackEntry(navDestinationId)
        navBackStackEntry.lifecycleScope.launch {
            navBackStackEntry.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.pendingNavigationCommandList
                    .collectLatest { value ->
                        if (value.isEmpty()) return@collectLatest

                        val firstPendingCommand = value.first()
                        if (!firstPendingCommand.canRetry()) {
                            Log.e(logTag, "保留ナビゲーションコマンド最大リトライ回数到達")
                            if (BuildConfig.DEBUG) throw IllegalStateException()
                            mainViewModel
                                .onPendingFragmentNavigationRetryLimitReached(firstPendingCommand)
                        }

                        val isNavigationSuccessful =
                            navigateFragmentOnce(
                                navController,
                                navDestinationId,
                                firstPendingCommand.command
                            )
                        if (isNavigationSuccessful) {
                            mainViewModel.onPendingFragmentNavigationComplete(firstPendingCommand)
                        } else {
                            mainViewModel.onPendingFragmentNavigationFailure(firstPendingCommand)
                        }
                    }
            }
        }
    }
    //endregion

    //region Navigation Helpers
    /**
     * 画面遷移を一度だけ試みる。
     * @param navController 画面遷移を管理するNavController
     * @param fragmentDestinationId 現在のフラグメントのDestination ID
     * @param command 実行するナビゲーションコマンド
     * @return 画面遷移が試みられた場合はtrue、そうでなければfalse
     */
    fun navigateFragmentOnce(
        navController: NavController,
        fragmentDestinationId: Int,
        command: NavigationCommand,
    ): Boolean {
        Log.d(logTag, "リトライなし画面遷移開始")
        return executeFragmentNavigation(
            navController,
            fragmentDestinationId,
            command
        )
    }

    /**
     * 画面遷移を試み、失敗した場合はViewModelに通知してリトライを可能にする。
     * @param navController 画面遷移を管理するNavController
     * @param fragmentDestinationId 現在のフラグメントのDestination ID
     * @param mainViewModel 失敗時にコマンドを保持するViewModel
     * @param command 実行するナビゲーションコマンド
     */
    fun navigateFragmentWithRetry(
        navController: NavController,
        fragmentDestinationId: Int,
        mainViewModel: BaseFragmentViewModel<out UiState, out UiEvent, out AppMessage>,
        command: NavigationCommand,
    ) {
        Log.d(logTag, "リトライあり画面遷移開始")
        executeFragmentNavigation(
            navController,
            fragmentDestinationId,
            command
        ) {
            mainViewModel.onFragmentNavigationFailure(command)
        }
    }

    /**
     * 画面遷移コマンドを実際に実行する内部関数。
     * @param navController 画面遷移を管理するNavController
     * @param fragmentDestinationId 現在のフラグメントのDestination ID
     * @param command 実行するナビゲーションコマンド
     * @param onCannotNavigate 画面遷移が不可能な場合に実行されるコールバック
     * @return 画面遷移が試みられた場合はtrue、そうでなければfalse
     */
    private fun executeFragmentNavigation(
        navController: NavController,
        fragmentDestinationId: Int,
        command: NavigationCommand,
        onCannotNavigate: () -> Unit = {}
    ): Boolean {
        if (!canNavigateFragment(navController, fragmentDestinationId)) {
            Log.d(logTag, "画面遷移不可_$command")
            onCannotNavigate()
            return false
        }

        Log.d(logTag, "画面遷移ナビゲーション起動_$command")
        when (command) {
            is NavigationCommand.To -> {
                navController.navigate(command.directions)
            }
            is NavigationCommand.Up<*> -> {
                val result = command.result
                if (result is FragmentResult.Some<*>) {
                    val previousBackStackEntry = checkNotNull(navController.previousBackStackEntry)
                    previousBackStackEntry.savedStateHandle[result.key] = result
                }
                navController.navigateUp()
            }
            is NavigationCommand.Pop<*> -> {
                val result = command.result
                if (result is FragmentResult.Some<*>) {
                    val previousBackStackEntry = checkNotNull(navController.previousBackStackEntry)
                    previousBackStackEntry.savedStateHandle[result.key] = result
                }
                navController.popBackStack()
            }
            is NavigationCommand.PopTo<*> -> {
                val result = command.result
                if (result is FragmentResult.Some<*>) {
                    val previousBackStackEntry = navController.getBackStackEntry(command.destinationId)
                    previousBackStackEntry.savedStateHandle[result.key] = result
                }
                navController.popBackStack(command.destinationId, command.inclusive)
            }
        }
        return true
    }

    /**
     * 現在の画面から指定されたコマンドで遷移可能か判定する。
     * @param navController 画面遷移を管理するNavController
     * @param fragmentDestinationId 現在いるべきフラグメントのDestination ID
     * @return 遷移可能な場合はtrue
     */
    private fun canNavigateFragment(
        navController: NavController,
        fragmentDestinationId: Int
    ): Boolean {
        val currentDestination = navController.currentDestination ?: return false
        return fragmentDestinationId == currentDestination.id
    }

    /**
     * 前の画面に一度だけ戻る。
     * @param navController 画面遷移を管理するNavController
     * @param fragmentDestinationId 現在のフラグメントのDestination ID
     * @param result 遷移元に渡す結果データ
     */
    fun navigatePreviousFragmentOnce(
        navController: NavController,
        fragmentDestinationId: Int,
        result: FragmentResult<*>
    ) {
        navigateFragmentOnce(
            navController,
            fragmentDestinationId,
            createPreviousFragmentCommand(result)
        )
    }

    /**
     * 前の画面に戻る遷移を試み、失敗した場合はリトライする。
     * @param navController 画面遷移を管理するNavController
     * @param fragmentDestinationId 現在のフラグメントのDestination ID
     * @param result 遷移元に渡す結果データ
     * @param mainViewModel 失敗時にコマンドを保持するViewModel
     */
    fun navigatePreviousFragmentWithRetry(
        navController: NavController,
        fragmentDestinationId: Int,
        result: FragmentResult<*>,
        mainViewModel: BaseFragmentViewModel<out UiState, out UiEvent, out AppMessage>,
    ) {
        navigateFragmentWithRetry(
            navController,
            fragmentDestinationId,
            mainViewModel,
            createPreviousFragmentCommand(result)
        )
    }

    /**
     * 前の画面に戻るためのNavigationCommandを生成する。
     * @param result 遷移元に渡す結果データ
     * @return 生成されたNavigationCommand
     */
    private fun createPreviousFragmentCommand(result: FragmentResult<*>): NavigationCommand {
         return NavigationCommand.Up(result)
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
