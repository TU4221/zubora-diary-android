package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.result.NavigationResult
import com.websarva.wings.android.zuboradiary.ui.model.state.UiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseViewModel
import kotlinx.coroutines.CoroutineScope

abstract class BaseFullScreenDialogFragment<T: ViewBinding, E: UiEvent>: BaseSimpleFullScreenDialogFragment<T>() {

    internal abstract val mainViewModel: BaseViewModel<E, out AppMessage, out UiState>

    internal abstract val destinationId: Int

    internal fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit
    ) {
        fragmentHelper.launchAndRepeatOnViewLifeCycleStarted(this, block)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFragmentResultReceiver()
        setUpUiEvent()
        setUpPendingNavigationCollector()
        registerOnBackPressedCallback()
    }

    /**
     *  setUpFragmentResultReceiver()、setUpDialogResultReceiver()を使用してフラグメント、ダイアログからの結果の処理内容を設定する
     * */
    internal abstract fun initializeFragmentResultReceiver()

    internal fun <T> setUpDialogResultReceiver(key: String, block: (DialogResult<T>) -> Unit) {
        setUpFragmentResultReceiverInternal(key, block)
    }

    private fun <R: NavigationResult> setUpFragmentResultReceiverInternal(
        key: String,
        block: (R) -> Unit
    ) {
        fragmentHelper
            .setUpFragmentResultReceiverInternal(
                findNavController(),
                destinationId,
                key,
                block
            )
    }

    private fun setUpUiEvent() {
        setUpMainUiEvent()
        setUpSettingsUiEvent()
    }

    private fun setUpMainUiEvent() {
        fragmentHelper
            .setUpMainUiEvent(
                this,
                mainViewModel,
                ::onMainUiEventReceived
            )
    }

    internal abstract fun onMainUiEventReceived(event: E)

    private fun setUpSettingsUiEvent() {
        fragmentHelper.setUpSettingsUiEvent(
            this,
            mainViewModel,
            settingsViewModel,
            ::navigateAppMessageDialog
        )
    }

    private fun setUpPendingNavigationCollector() {
        fragmentHelper
            .setUpPendingNavigationCollector(
                findNavController(),
                destinationId,
                mainViewModel
            )
    }

    internal fun navigateFragmentOnce(command: NavigationCommand) {
        fragmentHelper
            .navigateFragmentOnce(
                findNavController(),
                destinationId,
                command
            )
    }

    internal fun navigateFragmentWithRetry(command: NavigationCommand) {
        fragmentHelper
            .navigateFragmentWithRetry(
                findNavController(),
                destinationId,
                mainViewModel,
                command
            )
    }

    internal fun navigatePreviousFragment(
        resultKey: String? = null,
        result: FragmentResult<*> = FragmentResult.None
    ) {
        fragmentHelper
            .navigatePreviousFragmentOnce(
                findNavController(),
                destinationId,
                resultKey,
                result
            )
    }

    internal abstract fun navigateAppMessageDialog(appMessage: AppMessage)

    private fun registerOnBackPressedCallback() {
        fragmentHelper.registerOnBackPressedCallback(this, mainViewModel)
    }
}
