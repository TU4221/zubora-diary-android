package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.model.CalendarAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.action.CalendarFragmentAction
import com.websarva.wings.android.zuboradiary.ui.model.action.FragmentAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : BaseViewModel() {

    private val logTag = createLogTag()

    private val initialSelectedDate = LocalDate.now()
    private val _selectedDate = MutableStateFlow<LocalDate>(initialSelectedDate)
    val selectedDate get() = _selectedDate.asStateFlow()

    private val initialPreviousSelectedDate = null
    private val _previousSelectedDate = MutableStateFlow<LocalDate?>(initialPreviousSelectedDate)
    val previousSelectedDate get() = _previousSelectedDate.asStateFlow()

    // Fragment処理
    private val initialFragmentAction = FragmentAction.None
    private val _fragmentAction: MutableStateFlow<FragmentAction> =
        MutableStateFlow(initialFragmentAction)
    val fragmentAction: StateFlow<FragmentAction>
        get() = _fragmentAction

    override fun initialize() {
        super.initialize()
        _selectedDate.value = initialSelectedDate
        _previousSelectedDate.value = initialPreviousSelectedDate
        _fragmentAction.value = initialFragmentAction
    }

    fun onCalendarDayClicked(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSelectedDate(date)
            val exists = existsSavedDiary(date) ?: false
            if (exists) {
                _fragmentAction.value = CalendarFragmentAction.ShowDiary(date)
            } else {
                _fragmentAction.value = CalendarFragmentAction.CloseDiary
            }
        }
    }

    fun onDiaryEditButtonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            val date = _selectedDate.value
            val exists = existsSavedDiary(date) ?: false
            val isNewDiary = !exists
            _fragmentAction.value =
                CalendarFragmentAction.NavigateDiaryEditFragment(date, isNewDiary)
        }
    }

    fun prepareDiaryShowLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            val date = _selectedDate.value
            val exists = existsSavedDiary(date) ?: false
            if (exists) {
                _fragmentAction.value = CalendarFragmentAction.ShowDiary(date)
            } else {
                _fragmentAction.value = CalendarFragmentAction.CloseDiary
            }
        }
    }

    private fun updateSelectedDate(date: LocalDate) {
        // MEMO:selectedDateと同日付を選択した時、previousSelectedDateと同値となり、
        //      次に他の日付を選択した時にpreviousSelectedDateのcollectedが起動しなくなる。
        //      下記条件で対策。
        if (date == _selectedDate.value) return

        _previousSelectedDate.value = _selectedDate.value
        _selectedDate.value = date
    }

    suspend fun existsSavedDiary(date: LocalDate): Boolean? {
        try {
            return diaryRepository.existsDiary(date)
        } catch (e: Exception) {
            // MEMO:CalendarViewModel#hasDiary()はカレンダー日数分連続で処理する為、
            //      エラーが連続で発生した場合、膨大なエラーを記録してしまう。これを回避する為に下記コードを記述。
            if (equalLastAppMessage(CalendarAppMessage.DiaryLoadingFailure)) return false
            Log.e(logTag, "日記既存確認_失敗", e)
            addAppMessage(CalendarAppMessage.DiaryInfoLoadingFailure)
            return null
        }
    }

    fun clearFragmentAction() {
        _fragmentAction.value = initialFragmentAction
    }
}
