package com.websarva.wings.android.zuboradiary.ui.fragment

import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.result.NavigationResult
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.fragment.common.ActivityCallbackUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.fragment.common.CommonUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.fragment.common.MainUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.model.event.ActivityCallbackUiEvent
import com.websarva.wings.android.zuboradiary.ui.viewmodel.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class FragmentHelper {

    //region View Inflater Helpers
    fun createThemeColorInflater(
        inflater: LayoutInflater,
        themeColor: ThemeColorUi
    ): LayoutInflater {
        return ThemeColorInflaterCreator().create(inflater, themeColor)
    }
    //endregion

    //region UI Observation Helpers
    fun <R : NavigationResult> observeFragmentResult(
        navController: NavController,
        fragmentDestinationId: Int,
        key: String,
        block: (R) -> Unit
    ) {
        val navBackStackEntry = navController.getBackStackEntry(fragmentDestinationId)
        val savedStateHandle = navBackStackEntry.savedStateHandle
        val result = savedStateHandle.getStateFlow(key, null)

        // MEMO:対象デスティネーションがRESUMEDになった際に、他の画面からの結果を安全に受け取り処理するため、
        //      NavBackStackEntryのライフサイクルを使用。これにより、結果処理後の画面遷移も適切なタイミングで行える。
        navBackStackEntry.lifecycleScope.launch {
            navBackStackEntry.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                result.filterNotNull().collect { value: R ->
                    block(value)

                    savedStateHandle[key] = null
                }
            }
        }
    }

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

                    handler.onCommonUiEventReceived(event)
                }
        }
    }

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
                if (command.resultKey != null) {
                    val previousBackStackEntry = checkNotNull(navController.previousBackStackEntry)
                    previousBackStackEntry.savedStateHandle[command.resultKey] = command.result
                }
                navController.navigateUp()
            }
            is NavigationCommand.Pop<*> -> {
                if (command.resultKey != null) {
                    val previousBackStackEntry = checkNotNull(navController.previousBackStackEntry)
                    previousBackStackEntry.savedStateHandle[command.resultKey] = command.result
                }
                navController.popBackStack()
            }
            is NavigationCommand.PopTo<*> -> {
                if (command.resultKey != null) {
                    val previousBackStackEntry = navController.getBackStackEntry(command.destinationId)
                    previousBackStackEntry.savedStateHandle[command.resultKey] = command.result
                }
                navController.popBackStack(command.destinationId, command.inclusive)
            }
        }
        return true
    }

    private fun canNavigateFragment(
        navController: NavController,
        fragmentDestinationId: Int
    ): Boolean {
        val currentDestination = navController.currentDestination ?: return false
        return fragmentDestinationId == currentDestination.id
    }

    fun navigatePreviousFragmentOnce(
        navController: NavController,
        fragmentDestinationId: Int,
        resultKey: String?,
        result: FragmentResult<*>
    ) {
        navigateFragmentOnce(
            navController,
            fragmentDestinationId,
            createPreviousFragmentCommand(resultKey, result)
        )
    }

    fun navigatePreviousFragmentWithRetry(
        navController: NavController,
        fragmentDestinationId: Int,
        resultKey: String?,
        result: FragmentResult<*>,
        mainViewModel: BaseFragmentViewModel<out UiState, out UiEvent, out AppMessage>,
    ) {
        navigateFragmentWithRetry(
            navController,
            fragmentDestinationId,
            mainViewModel,
            createPreviousFragmentCommand(resultKey, result)
        )
    }

    private fun createPreviousFragmentCommand(
        resultKey: String?,
        result: FragmentResult<*>,
        ): NavigationCommand {
         return if (resultKey == null) {
             NavigationCommand.Up<Nothing>()
         } else {
             NavigationCommand.Up(resultKey, result)
         }
    }
    //endregion

    //region Back Press Helpers
    fun registerOnBackPressedCallback(
        fragment: Fragment,
        mainViewModel: BaseFragmentViewModel<out UiState, out UiEvent, out AppMessage>
    ) {
        fragment.requireActivity().onBackPressedDispatcher
            .addCallback(
                fragment.viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (fragment.viewLifecycleOwner
                                .lifecycle.currentState != Lifecycle.State.RESUMED) return

                        mainViewModel.onBackPressed()
                    }
                }
            )
    }
    //endregion

    //region Coroutine Helpers
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
