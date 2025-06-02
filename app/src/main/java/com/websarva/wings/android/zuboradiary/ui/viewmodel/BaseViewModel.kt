package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryItemTitleEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.SettingsAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.WordSearchAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.ConsumableEvent
import com.websarva.wings.android.zuboradiary.ui.model.event.ViewModelEvent
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

internal abstract class BaseViewModel : ViewModel() {

    private val logTag = createLogTag()

    private val _viewModelEvent = MutableSharedFlow<ConsumableEvent<ViewModelEvent>>(replay = 1)
    val viewModelEvent = _viewModelEvent.asSharedFlow()

    // 表示保留中Navigation
    private val _pendingNavigationCommand =
        MutableStateFlow<NavigationCommand>(NavigationCommand.None)

    open fun initialize() {
        Log.d(logTag, "initialize()")
    }

    protected suspend fun emitViewModelEvent(event: ViewModelEvent) {
        _viewModelEvent.emit(
            ConsumableEvent(event)
        )
    }

    protected suspend fun emitAppMessageEvent(appMessage: AppMessage) {
        if (checkAppMessageTargetType(appMessage)) throw IllegalArgumentException()

        emitViewModelEvent(
            ViewModelEvent.NavigateAppMessage(
                appMessage
            )
        )
    }

    private fun checkAppMessageTargetType(appMessage: AppMessage): Boolean {
        return when (appMessage) {
            is DiaryListAppMessage -> this@BaseViewModel is DiaryListViewModel
            is WordSearchAppMessage -> this@BaseViewModel is WordSearchViewModel
            is CalendarAppMessage -> this@BaseViewModel is CalendarViewModel
            is SettingsAppMessage -> this@BaseViewModel is SettingsViewModel
            is DiaryShowAppMessage -> this@BaseViewModel is DiaryShowViewModel
            is DiaryEditAppMessage -> this@BaseViewModel is DiaryEditViewModel
            is DiaryItemTitleEditAppMessage -> this@BaseViewModel is DiaryItemTitleEditViewModel
        }
    }

    abstract fun onBackPressed()

    val pendingNavigationCommand
        get() = _pendingNavigationCommand.asStateFlow()

    fun onFragmentNavigationFailed(command: NavigationCommand) {
        _pendingNavigationCommand.value = command
    }

    fun onPendingFragmentNavigationCompleted() {
        _pendingNavigationCommand.value = NavigationCommand.None
    }

    fun onPendingFragmentNavigationFailed() {
        _pendingNavigationCommand.value = NavigationCommand.None
    }
}
