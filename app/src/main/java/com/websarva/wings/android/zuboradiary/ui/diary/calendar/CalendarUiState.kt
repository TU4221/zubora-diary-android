package com.websarva.wings.android.zuboradiary.ui.diary.calendar

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.common.model.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.common.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.diary.common.state.BaseDiaryShowUiState
import com.websarva.wings.android.zuboradiary.ui.common.state.UiState
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * カレンダー画面のUI状態を表すデータクラス。
 *
 * @property calendarStartDayOfWeek カレンダーの週の開始曜日。
 * @property existingDiaryDates 日記が存在する日付。高速検索用として [Set] で保持。
 * @property selectedDate ユーザーによって選択されている日付。
 * @property previousSelectedDate 以前（一つ前）に選択されていた日付。
 * @property diaryLoadState 日記データの読み込み状態。
 * @property diaryImageFilePath 添付画像のファイルパス。
 *
 * @property isWeather2Visible 天気2が表示されるべきかを示す。
 * @property numVisibleDiaryItems 表示されている日記項目の数。
 *
 * @property isProcessing 処理中（読み込み中など）であるかを示す。
 * @property isInputDisabled ユーザーの入力が無効化されているかを示す。
 */
@Parcelize
data class CalendarUiState(
    // UiData
    val calendarStartDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    val existingDiaryDates: Set<LocalDate> = emptySet(),
    val selectedDate: LocalDate = LocalDate.now(),
    val previousSelectedDate: LocalDate? = null,
    override val diaryLoadState: LoadState<DiaryUi> = LoadState.Idle,
    override val diaryImageFilePath: FilePathUi? = null,

    // UiState
    override val isWeather2Visible: Boolean = false,
    override val numVisibleDiaryItems: Int = 1,

    // ProcessingState
    override val isProcessing: Boolean = false,
    override val isInputDisabled: Boolean = false
) : UiState, BaseDiaryShowUiState, Parcelable {

    companion object {
        fun fromSavedState(savedUiState: CalendarUiState): CalendarUiState {
            return savedUiState.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }
}
