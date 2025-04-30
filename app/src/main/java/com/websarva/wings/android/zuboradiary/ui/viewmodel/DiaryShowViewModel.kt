package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.model.ItemNumber
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.DiaryShowPendingDialog
import com.websarva.wings.android.zuboradiary.ui.model.navigation.DiaryShowNavigationAction
import com.websarva.wings.android.zuboradiary.ui.model.navigation.NavigationAction
import com.websarva.wings.android.zuboradiary.ui.permission.UriPermissionAction
import com.websarva.wings.android.zuboradiary.ui.utils.requireValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class DiaryShowViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {

    private val logTag = createLogTag()

    // 日記データ関係
    private val diaryStateFlow = DiaryStateFlow()
    val date
        get() = diaryStateFlow.date.asStateFlow()
    val weather1
        get() = diaryStateFlow.weather1.asStateFlow()
    val weather2
        get() = diaryStateFlow.weather2.asStateFlow()
    val condition
        get() = diaryStateFlow.condition.asStateFlow()
    val title
        get() = diaryStateFlow.title.asStateFlow()
    val numVisibleItems
        get() = diaryStateFlow.numVisibleItems.asStateFlow()
    val item1Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(1)).title.asStateFlow()
    val item2Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(2)).title.asStateFlow()
    val item3Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(3)).title.asStateFlow()
    val item4Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(4)).title.asStateFlow()
    val item5Title
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(5)).title.asStateFlow()
    val item1Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(1)).comment.asStateFlow()
    val item2Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(2)).comment.asStateFlow()
    val item3Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(3)).comment.asStateFlow()
    val item4Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(4)).comment.asStateFlow()
    val item5Comment
        get() = diaryStateFlow.getItemStateFlow(ItemNumber(5)).comment.asStateFlow()
    val picturePath
        get() = diaryStateFlow.picturePath.asStateFlow()
    val log
        get() = diaryStateFlow.log.asStateFlow()

    // Fragment表示
    private val initialNavigationAction = NavigationAction.None
    private val _navigationAction: MutableStateFlow<NavigationAction> =
        MutableStateFlow(initialNavigationAction)
    val navigationAction: StateFlow<NavigationAction>
        get() = _navigationAction

    override fun initialize() {
        super.initialize()
        diaryStateFlow.initialize()
    }

    fun loadSavedDiary(date: LocalDate, ignoreAppMessage: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val logMsg = "日記読込"
            Log.i(logTag, "${logMsg}_開始")

            try {
                val diaryEntity = diaryRepository.loadDiary(date) ?: throw IllegalArgumentException()
                diaryStateFlow.update(diaryEntity)
            } catch (e: Exception) {
                Log.e(logTag, "${logMsg}_失敗", e)
                if (ignoreAppMessage) {
                    _navigationAction.value =
                        DiaryShowNavigationAction.NavigateDiaryLoadingFailureDialog(date)
                } else {
                    addAppMessage(DiaryShowAppMessage.DiaryLoadingFailure)
                }
            }

            Log.i(logTag, "${logMsg}_完了")
        }
    }

    fun deleteDiary() {
        viewModelScope.launch(Dispatchers.IO) {
            val logMsg = "日記削除"
            Log.i(logTag, "${logMsg}_開始")

            val deleteDate = diaryStateFlow.date.requireValue()
            val picturePath  = diaryStateFlow.picturePath.value
            try {
                diaryRepository.deleteDiary(deleteDate)
            } catch (e: Exception) {
                Log.e(logTag, "${logMsg}_失敗", e)
                addAppMessage(DiaryShowAppMessage.DiaryDeleteFailure)
                return@launch
            }

            if (picturePath == null) {
                _navigationAction.value = NavigationAction.NavigatePreviousFragment
            } else {
                _navigationAction.value =
                    DiaryShowNavigationAction
                        .NavigatePreviousDialogOnDiaryDelete(
                            UriPermissionAction.Release(picturePath)
                        )
            }
            Log.i(logTag, "${logMsg}_完了")
        }
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    suspend fun checkSavedPicturePathDoesNotExist(uri: Uri): Boolean? {
        try {
            return !diaryRepository.existsPicturePath(uri)
        } catch (e: Exception) {
            Log.e(logTag, "端末写真URI使用状況確認_失敗", e)
            addAppMessage(DiaryShowAppMessage.DiaryLoadingFailure)
            return null
        }
    }

    fun clearNavigationAction() {
        _navigationAction.value = initialNavigationAction
    }

    // 表示保留中Dialog追加
    // MEMO:引数の型をサブクラスに制限
    fun addPendingDialogList(pendingDialog: DiaryShowPendingDialog) {
        super.addPendingDialogList(pendingDialog)
    }
}
