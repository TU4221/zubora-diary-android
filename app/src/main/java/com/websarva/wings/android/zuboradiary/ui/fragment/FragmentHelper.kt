package com.websarva.wings.android.zuboradiary.ui.fragment

import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.CommonUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.SettingsEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import com.websarva.wings.android.zuboradiary.ui.theme.ThemeColorInflaterCreator
import com.websarva.wings.android.zuboradiary.ui.viewmodel.BaseViewModel
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
        themeColor: ThemeColor
    ): LayoutInflater {
        return ThemeColorInflaterCreator().create(inflater, themeColor)
    }

    fun <R> setUpFragmentResultReceiverInternal(
        fragment: Fragment,
        navController: NavController,
        fragmentDestinationId: Int,
        key: String,
        block: (R) -> Unit
    ) {
        val backStackEntry = navController.getBackStackEntry(fragmentDestinationId)
        val savedStateHandle = backStackEntry.savedStateHandle
        val result = savedStateHandle.getStateFlow(key, null)
        launchAndRepeatOnViewLifeCycleStarted(fragment) {
            result.filterNotNull().collectLatest { value: R ->
                block(value)

                savedStateHandle[key] = null
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
                            when (event.event) {
                                is CommonUiEvent.NavigateAppMessage -> {
                                    onAppMessageNavigationUiEventReceived(event.event.message)
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
        mainViewModel: BaseViewModel<out UiEvent, out AppMessage, out UiState>,
        onNavigationCommandReceived: (NavigationCommand) -> Unit
    ) {
        val navBackStackEntry = navController.getBackStackEntry(navDestinationId)
        navBackStackEntry.lifecycleScope.launch {
            navBackStackEntry.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.pendingNavigationCommand
                    .collectLatest { value ->
                        when (value) {
                            NavigationCommand.None -> {
                                // 処理なし
                            }
                            else -> {
                                if (!canNavigateFragment(navController, navDestinationId)) {
                                    mainViewModel.onPendingFragmentNavigationFailed()
                                    return@collectLatest
                                }

                                onNavigationCommandReceived(value)
                                mainViewModel.onPendingFragmentNavigationCompleted()
                            }
                        }
                    }
            }
        }
    }

    fun navigateFragment(
        navController: NavController,
        fragmentDestinationId: Int,
        mainViewModel: BaseViewModel<out UiEvent, out AppMessage, out UiState>,
        command: NavigationCommand,
    ) {
        if (!canNavigateFragment(navController, fragmentDestinationId)) {
            mainViewModel.onFragmentNavigationFailed(command)
            return
        }

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
            NavigationCommand.None -> {
                // 処理なし
            }
        }
    }

    private fun canNavigateFragment(
        navController: NavController,
        fragmentDestinationId: Int
    ): Boolean {
        val currentDestination = navController.currentDestination ?: return false
        return fragmentDestinationId == currentDestination.id
    }

    fun navigatePreviousFragment(
        navController: NavController,
        fragmentDestinationId: Int,
        mainViewModel: BaseViewModel<out UiEvent, out AppMessage, out UiState>,
        resultKey: String?,
        result: FragmentResult<*>
    ) {
        val command =
            if (resultKey == null) {
                NavigationCommand.Up()
            } else {
                NavigationCommand.Up(resultKey, result)
            }
        navigateFragment(
            navController,
            fragmentDestinationId,
            mainViewModel,
            command
        )
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
                        mainViewModel.onBackPressed()
                    }
                }
            )
    }
}
