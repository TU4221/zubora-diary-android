package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.websarva.wings.android.zuboradiary.ui.model.AppMessageList
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal abstract class BaseViewModel : ViewModel() {

    private val logTag = createLogTag()

    // MEMO:ViewModelのスコープ範囲がActivityの時に各プロパティを初期化できるように抽象メソッドinitialize()を用意しているが、
    //      appMessageBufferListに関しては主にエラー表示となるため、ViewModelの初期化時のみの初期化する。
    private val _appMessageBufferList = MutableStateFlow(AppMessageList())
    val appMessageBufferList
        get() = _appMessageBufferList.asStateFlow()

    // 表示保留中Navigation
    private val _pendingNavigationCommand =
        MutableStateFlow<NavigationCommand>(NavigationCommand.None)

    open fun initialize() {
        Log.d(logTag, "initialize()")
    }

    protected fun addAppMessage(appMessage: AppMessage) {
        Log.d(logTag, "addAppMessage()")
        val currentList = requireNotNull(_appMessageBufferList.value)
        val updateList = currentList.add(appMessage)
        _appMessageBufferList.value = updateList
    }

    fun triggerAppMessageBufferListObserver() {
        Log.d(logTag, "triggerAppMessageBufferListObserver()")
        val currentList = _appMessageBufferList.value
        _appMessageBufferList.value = AppMessageList(currentList)
    }

    fun removeAppMessageBufferListFirstItem() {
        Log.d(logTag, "removeAppMessageBufferListFirstItem()")
        val currentList = _appMessageBufferList.value
        val updateList = currentList.removeFirstItem()
        _appMessageBufferList.value = updateList
    }

    protected fun equalLastAppMessage(appMessage: AppMessage): Boolean {
        Log.d(logTag, "equalLastAppMessage()")
        val currentList = _appMessageBufferList.value
        return currentList.equalLastItem(appMessage)
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
