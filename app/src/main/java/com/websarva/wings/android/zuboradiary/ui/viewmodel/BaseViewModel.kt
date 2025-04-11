package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.websarva.wings.android.zuboradiary.ui.model.AppMessageList
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.PendingDialog
import com.websarva.wings.android.zuboradiary.ui.model.PendingDialogList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {

    private val logTag = createLogTag()

    // MEMO:ViewModelのスコープ範囲がActivityの時に各プロパティを初期化できるように抽象メソッドinitialize()を用意しているが、
    //      appMessageBufferListに関しては主にエラー表示となるため、ViewModelの初期化時のみの初期化する。
    private val _appMessageBufferList = MutableStateFlow(AppMessageList())
    val appMessageBufferList
        get() = _appMessageBufferList.asStateFlow()

    // 表示保留中Dialog
    private val initialPendingDialogList = PendingDialogList()
    private val _pendingDialogList = MutableStateFlow(initialPendingDialogList)
    val pendingDialogList
        get() = _pendingDialogList.asStateFlow()

    open fun initialize() {
        Log.d(logTag, "initialize()")
        _pendingDialogList.value = initialPendingDialogList
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

    // 表示保留中Dialog
    // HACK:特定のViewModelからPendingDialogを追加するときは対象のPendingDialogのみを受取たい為、
    //      下記メソッドのアクセス修飾子をprotectedにしてサブクラスに引数の制限を託す。
    /**
     * 本メソッドを本クラス以外のクラス(Fragment等)から使用する場合は、サブクラスで本メソッドを呼び出すメソッドを作成すること。
     * 作成したメソッドの引数の型は対象のPendingDialogクラスのサブクラスを指定すること。
     * */
    protected fun addPendingDialogList(pendingDialog: PendingDialog) {
        Log.d(logTag, "addPendingDialogList()")
        val currentList = _pendingDialogList.value
        val updateList = currentList.add(pendingDialog)
        _pendingDialogList.value = updateList
    }

    fun triggerPendingDialogListObserver() {
        Log.d(logTag, "triggerPendingDialogListObserver()")
        val currentList = _pendingDialogList.value
        _pendingDialogList.value = PendingDialogList(currentList)
    }

    fun removePendingDialogListFirstItem() {
        Log.d(logTag, "removePendingDialogListFirstItem()")
        val currentList = _pendingDialogList.value
        val updateList = currentList.removeFirstItem()
        _pendingDialogList.value = updateList
    }
}
