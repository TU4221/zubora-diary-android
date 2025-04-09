package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.createLogTag
import com.websarva.wings.android.zuboradiary.data.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.DiaryShowPendingDialog
import com.websarva.wings.android.zuboradiary.ui.requireValue
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
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

    override fun initialize() {
        super.initialize()
        diaryStateFlow.initialize()
    }

    suspend fun loadSavedDiary(date: LocalDate, ignoreAppMessage: Boolean = false): Boolean {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        try {
            val diaryEntity = diaryRepository.loadDiary(date) ?: throw IllegalArgumentException()
            diaryStateFlow.update(diaryEntity)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            if (!ignoreAppMessage) {
                addAppMessage(DiaryShowAppMessage.DiaryLoadingFailure)
            }
            return false
        }

        Log.i(logTag, "${logMsg}_完了")
        return true
    }

    suspend fun deleteDiary(): Boolean {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        val deleteDate = diaryStateFlow.date.requireValue()
        try {
            diaryRepository.deleteDiary(deleteDate)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            addAppMessage(DiaryShowAppMessage.DiaryDeleteFailure)
            return false
        }

        Log.i(logTag, "${logMsg}_完了")
        return true
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

    // 表示保留中Dialog追加
    // MEMO:引数の型をサブクラスに制限
    fun addPendingDialogList(pendingDialog: DiaryShowPendingDialog) {
        super.addPendingDialogList(pendingDialog)
    }
}
