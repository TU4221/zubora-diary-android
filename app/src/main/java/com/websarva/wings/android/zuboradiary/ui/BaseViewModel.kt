package com.websarva.wings.android.zuboradiary.ui

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
        _appMessageBufferList.value = initialAppMessageBufferList
    }

    /**
     * 本メソッドは継承先のクラス毎に処理するタイミングが異なるので、Override後、継承クラスのコンストラクタに含めること。
     */
    protected abstract fun initialize()

    protected fun addAppMessage(appMessage: AppMessage) {
        val currentList = requireNotNull(_appMessageBufferList.value)
        val updateList = currentList.add(appMessage)
        _appMessageBufferList.value = updateList
    }

    fun triggerAppMessageBufferListObserver() {
        val currentList = checkNotNull(_appMessageBufferList.value)
        _appMessageBufferList.value = AppMessageList()
        _appMessageBufferList.value = currentList
    }

    fun removeAppMessageBufferListFirstItem() {
        val currentList = checkNotNull(_appMessageBufferList.value)
        val updateList = currentList.removeFirstItem()
        _appMessageBufferList.value = updateList
    }

    protected fun equalLastAppMessage(appMessage: AppMessage): Boolean {
        val currentList = checkNotNull(_appMessageBufferList.value)
        return currentList.equalLastItem(appMessage)
    }
}
