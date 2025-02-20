package com.websarva.wings.android.zuboradiary.ui.diary.diaryshow

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.diary.Condition
import com.websarva.wings.android.zuboradiary.data.diary.ItemNumber
import com.websarva.wings.android.zuboradiary.data.diary.Weather
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import com.websarva.wings.android.zuboradiary.ui.checkNotNull
import com.websarva.wings.android.zuboradiary.ui.diary.DiaryLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DiaryShowViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {

    // 日記データ関係
    private val diaryLiveData = DiaryLiveData()
    val dateLiveData: LiveData<LocalDate?> get() = diaryLiveData.date
    val weather1LiveData: LiveData<Weather> get() = diaryLiveData.weather1
    val weather2LiveData: LiveData<Weather> get() = diaryLiveData.weather2
    val conditionLiveData: LiveData<Condition> get() = diaryLiveData.condition
    val titleLiveData: LiveData<String> get() = diaryLiveData.title
    val numVisibleItemsLiveData: LiveData<Int> get() = diaryLiveData.numVisibleItems
    val item1TitleLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(1)).title
    val item2TitleLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(2)).title
    val item3TitleLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(3)).title
    val item4TitleLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(4)).title
    val item5TitleLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(5)).title
    val item1CommentLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(1)).comment
    val item2CommentLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(2)).comment
    val item3CommentLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(3)).comment
    val item4CommentLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(4)).comment
    val item5CommentLiveData: LiveData<String>
        get() = diaryLiveData.getItemLiveData(ItemNumber(5)).comment
    val picturePathLiveData: LiveData<Uri?> get() = diaryLiveData.picturePath
    val logLiveData: LiveData<LocalDateTime?> get() = diaryLiveData.log

    init {
        initialize()
    }

    public override fun initialize() {
        initializeAppMessageList()
        diaryLiveData.initialize()
    }

    suspend fun existsSavedDiary(date: LocalDate): Boolean? {
        try {
            return diaryRepository.existsDiary(date)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        }
    }

    suspend fun loadSavedDiary(date: LocalDate): Boolean {
        try {
            val diaryEntity = diaryRepository.loadDiary(date)
            diaryLiveData.update(diaryEntity)
        } catch (e: Exception) {
            Log.d("Exception", "loadSavedDiary()", e)
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return false
        }
        return true
    }

    suspend fun deleteDiary(): Boolean {
        val deleteDate = diaryLiveData.date.checkNotNull()
        try {
            diaryRepository.deleteDiary(deleteDate)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_DELETE_ERROR)
            return false
        }
        return true
    }

    // MEMO:存在しないことを確認したいため下記メソッドを否定的処理とする
    suspend fun checkSavedPicturePathDoesNotExist(uri: Uri): Boolean? {
        try {
            return !diaryRepository.existsPicturePath(uri)
        } catch (e: Exception) {
            addAppMessage(AppMessage.DIARY_LOADING_ERROR)
            return null
        }
    }
}
