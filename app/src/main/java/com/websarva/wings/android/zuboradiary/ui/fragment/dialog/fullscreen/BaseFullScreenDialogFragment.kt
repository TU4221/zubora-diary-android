package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.fullscreen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.websarva.wings.android.zuboradiary.ui.fragment.common.CommonUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.fragment.common.MainUiEventHandler
import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.UiEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.UiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import kotlinx.coroutines.CoroutineScope

abstract class BaseFullScreenDialogFragment<T: ViewBinding, E: UiEvent>
    : BaseSimpleFullScreenDialogFragment<T>(), MainUiEventHandler<E>, CommonUiEventHandler {

    //region Properties
    protected abstract val mainViewModel: BaseFragmentViewModel<out UiState, E, out AppMessage>

    protected abstract val destinationId: Int
    //endregion

    //region Fragment Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpFragmentResultObservers()
        setUpUiEventObservers()
        observePendingNavigation()
        registerOnBackPressedCallback()
    }
    //endregion

    //region Fragment Result Observation Setup
    /**
     *  setUpFragmentResultReceiver()、setUpDialogResultReceiver()を使用してフラグメント、ダイアログからの結果の処理内容を設定する
     * */
    protected abstract fun setUpFragmentResultObservers()

    protected fun <T> observeDialogResult(key: String, block: (DialogResult<T>) -> Unit) {
        fragmentHelper
            .observeFragmentResult(
                findNavController(),
                destinationId,
                key,
                block
            )
    }
    //endregion

    //region UI Observation Setup
    private fun setUpUiEventObservers() {
        observeMainUiEvent()
        observeCommonUiEvent()
    }

    private fun observeMainUiEvent() {
        fragmentHelper
            .observeMainUiEvent(
                this,
                mainViewModel,
                this
            )
    }

    private fun observeCommonUiEvent() {
        fragmentHelper
            .observeCommonUiEvent(
                this,
                mainViewModel,
                this
            )
    }

    private fun observePendingNavigation() {
        fragmentHelper
            .observePendingNavigation(
                findNavController(),
                destinationId,
                mainViewModel
            )
    }
    //endregion

    //region Back Press Setup
    private fun registerOnBackPressedCallback() {
        fragmentHelper.registerOnBackPressedCallback(this, mainViewModel)
    }
    //endregion

    //region Navigation Helpers
    protected fun navigateFragmentOnce(command: NavigationCommand) {
        fragmentHelper
            .navigateFragmentOnce(
                findNavController(),
                destinationId,
                command
            )
    }

    protected fun navigateFragmentWithRetry(command: NavigationCommand) {
        fragmentHelper
            .navigateFragmentWithRetry(
                findNavController(),
                destinationId,
                mainViewModel,
                command
            )
    }

    protected fun navigatePreviousFragment(
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

    protected abstract fun navigateAppMessageDialog(appMessage: AppMessage)
    //endregion

    //region Internal Helpers
    internal fun launchAndRepeatOnViewLifeCycleStarted(
        block: suspend CoroutineScope.() -> Unit
    ) {
        fragmentHelper.launchAndRepeatOnViewLifeCycleStarted(this, block)
    }
    //endregion
}
