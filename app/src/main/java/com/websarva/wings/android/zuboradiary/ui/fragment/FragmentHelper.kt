package com.websarva.wings.android.zuboradiary.ui.fragment

import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.squareup.leakcanary.core.BuildConfig
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.SettingsEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.result.NavigationResult
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.SettingsViewModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal class FragmentHelper {

    val logTag = createLogTag()

    fun launchAndRepeatOnViewLifeCycleStarted(
        fragment: Fragment,
        block: suspend CoroutineScope.() -> Unit
    ) {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    fun createThemeColorInflater(
        inflater: LayoutInflater,
        themeColor: ThemeColorUi
    ): LayoutInflater {
        return ThemeColorInflaterCreator().create(inflater, themeColor)
    }

    fun <R : NavigationResult> setUpFragmentResultReceiverInternal(
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
                result.filterNotNull().collectLatest { value: R ->
                    block(value)

                    savedStateHandle[key] = null
                }
            }
        }
    }

    fun <E: UiEvent> setUpMainUiEvent(
        fragment: Fragment,
        mainViewModel: BaseViewModel<E, out AppMessage, out UiState>,
        onUiEventReceived: (E) -> Unit
    ) {
        launchAndRepeatOnViewLifeCycleStarted(fragment) {
            mainViewModel.uiEvent
                .collect { value: ConsumableEvent<E> ->
                    val event = value.getContentIfNotHandled()
                    Log.d(logTag, "UiEvent_Collect(): $event")
                    event ?: return@collect

                    onUiEventReceived(event)
                }
        }
    }

    fun setUpSettingsUiEvent(
        fragment: Fragment,
        mainViewModel: BaseViewModel<out UiEvent, out AppMessage, out UiState>,
        settingsViewModel: SettingsViewModel,
        onAppMessageNavigationUiEventReceived: (AppMessage) -> Unit
    ) {
        if (mainViewModel == settingsViewModel) return

        launchAndRepeatOnViewLifeCycleStarted(fragment) {
            settingsViewModel.uiEvent
                .collect { value: ConsumableEvent<SettingsEvent> ->
                    val event = value.getContentIfNotHandled()
                    Log.d(logTag, "SettingsUiEvent_Collect(): $event")
                    event ?: return@collect
                    when (event) {
                        is SettingsEvent.CommonEvent -> {
                            when (event.wrappedEvent) {
                                is CommonUiEvent.NavigateAppMessage -> {
                                    onAppMessageNavigationUiEventReceived(event.wrappedEvent.message)
                                }
                                else -> {
                                    throw IllegalArgumentException()
                                }
                            }
                        }
                        else -> {
                            throw IllegalArgumentException()
                        }
                    }
                }
        }
    }

    fun setUpPendingNavigationCollector(
        navController: NavController,
        navDestinationId: Int,
        mainViewModel: BaseViewModel<out UiEvent, out AppMessage, out UiState>
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
                            // TODO:DEBUGの設定を行う
                            if (BuildConfig.DEBUG) throw IllegalStateException()
                            return@collectLatest
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
        mainViewModel: BaseViewModel<out UiEvent, out AppMessage, out UiState>,
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
        mainViewModel: BaseViewModel<out UiEvent, out AppMessage, out UiState>,
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

    fun registerOnBackPressedCallback(
        fragment: Fragment,
        mainViewModel: BaseViewModel<out UiEvent, out AppMessage, out UiState>
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
}
