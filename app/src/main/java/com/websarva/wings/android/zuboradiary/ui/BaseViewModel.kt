package com.websarva.wings.android.zuboradiary.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {

    private val initialAppMessageBufferList = AppMessageList()
    private val _appMessageBufferList = MutableStateFlow(initialAppMessageBufferList)
    val appMessageBufferList
        get() = _appMessageBufferList.asStateFlow()

    init {
        initializeAppMessageList()
    }

    protected fun initializeAppMessageList() {
        Log.d(javaClass.simpleName, "initializeAppMessageList()")
        _appMessageBufferList.value = initialAppMessageBufferList
    }

    protected fun addAppMessage(appMessage: AppMessage) {
        Log.d(javaClass.simpleName, "addAppMessage()")
        val currentList = requireNotNull(_appMessageBufferList.value)
        val updateList = currentList.add(appMessage)
        _appMessageBufferList.value = updateList
    }

    fun triggerAppMessageBufferListObserver() {
        Log.d(javaClass.simpleName, "triggerAppMessageBufferListObserver()")
        val currentList = _appMessageBufferList.value
        _appMessageBufferList.value = AppMessageList()
        _appMessageBufferList.value = currentList
    }

    fun removeAppMessageBufferListFirstItem() {
        Log.d(javaClass.simpleName, "removeAppMessageBufferListFirstItem()")
        val currentList = _appMessageBufferList.value
        val updateList = currentList.removeFirstItem()
        _appMessageBufferList.value = updateList
    }

    protected fun equalLastAppMessage(appMessage: AppMessage): Boolean {
        Log.d(javaClass.simpleName, "equalLastAppMessage()")
        val currentList = _appMessageBufferList.value
        return currentList.equalLastItem(appMessage)
    }
}
