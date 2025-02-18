package com.websarva.wings.android.zuboradiary.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.websarva.wings.android.zuboradiary.data.AppMessage
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class CalendarViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    BaseViewModel() {

    private val _selectedDate = MutableLiveData<LocalDate>()
    internal val selectedDate: LiveData<LocalDate> get() = _selectedDate

    private val _previousSelectedDate = MutableLiveData<LocalDate?>()
    internal val previousSelectedDate: LiveData<LocalDate?> get() = _previousSelectedDate

    init {
        initialize()
    }

    override fun initialize() {
        initializeAppMessageList()
        _selectedDate.value = LocalDate.now()
        _previousSelectedDate.value = null
    }

    suspend fun existsSavedDiary(date: LocalDate): Boolean? {
        try {
            return diaryRepository.existsDiary(date)
        } catch (e: Exception) {
            // MEMO:CalendarViewModel#hasDiary()はカレンダー日数分連続で処理する為、
            //      エラーが連続で発生した場合、膨大なエラーを記録してしまう。これを回避する為に下記コードを記述。
            if (equalLastAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)) return false
            addAppMessage(AppMessage.DIARY_INFO_LOADING_ERROR)
            return null
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        _previousSelectedDate.value = _selectedDate.value
        _selectedDate.value = date
    }
}
