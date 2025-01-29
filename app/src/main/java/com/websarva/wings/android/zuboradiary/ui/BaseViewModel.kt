package com.websarva.wings.android.zuboradiary.ui

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.AppMessageList

abstract class BaseViewModel : ViewModel() {

    private val _appMessageBufferList = MutableLiveData<AppMessageList>()
    val appMessageBufferList: LiveData<AppMessageList>
        get() = _appMessageBufferList

    init {
        initializeAppMessageList()
    }

    protected fun initializeAppMessageList() {
        _appMessageBufferList.value = AppMessageList()
    }

    /**
     * 本メソッドは継承先のクラス毎に処理するタイミングが異なるので、Override後、継承クラスのコンストラクタに含めること。
     */
    protected abstract fun initialize()

    protected fun addAppMessage(appMessage: AppMessage) {
        val currentList = requireNotNull(_appMessageBufferList.value)
        val updateList = currentList.add(appMessage)

        val isMainThread = (Looper.getMainLooper().thread === Thread.currentThread())
        if (isMainThread) {
            _appMessageBufferList.setValue(updateList)
        } else {
            _appMessageBufferList.postValue(updateList)
        }
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
