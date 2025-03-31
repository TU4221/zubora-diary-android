package com.websarva.wings.android.zuboradiary.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import com.websarva.wings.android.zuboradiary.getLogTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {

    private val logTag = getLogTag()

    // MEMO:ViewModelのスコープ範囲がActivityの時に各プロパティを初期化できるように抽象メソッドinitialize()を用意しているが、
    //      appMessageBufferListに関しては主にエラー表示となるため、ViewModelの初期化時のみの初期化する。
    private val _appMessageBufferList = MutableStateFlow(AppMessageList())
    val appMessageBufferList
        get() = _appMessageBufferList.asStateFlow()

    open fun initialize() {
        Log.d(logTag, "addAppMessage()")
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
        _appMessageBufferList.value = AppMessageList()
        _appMessageBufferList.value = currentList
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
}
