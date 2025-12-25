package com.websarva.wings.android.zuboradiary.ui.diary.calendar

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.LoadExistingDiaryDateListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception.ExistingDiaryDateListLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingLoadException
import com.websarva.wings.android.zuboradiary.ui.diary.common.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.common.model.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.common.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.DummyNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.common.viewmodel.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.ui.diary.common.viewmodel.DiaryUiStateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * カレンダー画面のUIロジックと状態([CalendarUiState])管理を担うViewModel。
 *
 * 以下の責務を持つ:
 * - カレンダーの表示設定（週の開始曜日など）の読み込み
 * - ユーザーによって選択された日付に対応する日記データの有無を確認し、読み込む
 * - 読み込んだ日記データをUI表示用に加工し、UI状態を更新する
 * - 日記の有無に応じてカレンダーの日付へのドット表示を制御する
 * - 日記編集画面や前の画面への遷移イベントを発行する
 * - [SavedStateHandle]を利用して、プロセスの再生成後もUI状態を復元する
 */
@HiltViewModel
class CalendarViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val diaryUiStateHelper: DiaryUiStateHelper,
    private val loadCalendarStartDayOfWeekSettingUseCase: LoadCalendarStartDayOfWeekSettingUseCase,
    private val loadExistingDiaryDateListUseCase: LoadExistingDiaryDateListUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val loadDiaryByDateUseCase: LoadDiaryByDateUseCase
) : BaseFragmentViewModel<
        CalendarUiState,
        CalendarUiEvent,
        CalendarNavDestination,
        DummyNavBackDestination
        >(
    handle.get<CalendarUiState>(SAVED_STATE_UI_KEY)?.let {
        CalendarUiState.fromSavedState(it)
    } ?: CalendarUiState()
) {

    //region Properties
    /** このViewModelが操作を受け入れ可能な状態かを示す。 */
    override val isReadyForOperation
        get() = !currentUiState.isInputDisabled
                && (currentUiState.diaryLoadState is LoadState.Success
                        || currentUiState.diaryLoadState is LoadState.Empty)

    /** UI状態から正常に読み込まれた日記データのみを抽出して提供するFlow。 */
    private val diaryFlow =
        uiState.distinctUntilChanged { old, new ->
            old.diaryLoadState == new.diaryLoadState
        }.mapNotNull { (it.diaryLoadState as? LoadState.Success)?.data }

    /** カレンダーのスクロールを抑制するかを示すフラグ。 */
    private var suppressNextScroll = false
    //endregion

    //region Initialization
    init {
        collectUiStates()
    }

    /** UI状態の監視を開始する。 */
    private fun collectUiStates() {
        collectUiStateForSaveStateToHandle()
        collectCalendarStartDayOfWeekSetting()
        collectExistingDiaryDates()
        collectSelectedDate()
        collectWeather2Visible()
        collectNumVisibleDiaryItems()
        collectImageFilePath()
    }

    /** UI状態を[SavedStateHandle]に保存する。 */
    private fun collectUiStateForSaveStateToHandle() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    /** 週の開始曜日設定の変更を監視し、UIに反映させる。 */
    private fun collectCalendarStartDayOfWeekSetting() {
        loadCalendarStartDayOfWeekSettingUseCase()
            .onEach {
                when (it) {
                    is UseCaseResult.Success -> { /*処理なし*/ }
                    is UseCaseResult.Failure -> {
                        when (it.exception) {
                            is CalendarStartDayOfWeekSettingLoadException.LoadFailure -> {
                                showAppMessageDialog(CalendarAppMessage.SettingsLoadFailure)
                            }
                            is CalendarStartDayOfWeekSettingLoadException.Unknown -> {
                                showUnexpectedAppMessageDialog(it.exception)
                            }
                        }
                    }
                }
            }.map {
                when (it) {
                    is UseCaseResult.Success -> it.value
                    is UseCaseResult.Failure -> it.exception.fallbackSetting
                }
            }.catchUnexpectedError(
                CalendarStartDayOfWeekSetting.default()
            ).distinctUntilChanged().onEach { setting ->
                updateCalendarStartDayOfWeek(setting.dayOfWeek)
            }.launchIn(viewModelScope)
    }

    /** 日記が存在する日付のリストの変更を監視し、UIに反映させる。 */
    private fun collectExistingDiaryDates() {
        loadExistingDiaryDateListUseCase().onEach {
            when (it) {
                is UseCaseResult.Success -> { /*処理なし*/ }
                is UseCaseResult.Failure -> {
                    when (it.exception) {
                        is ExistingDiaryDateListLoadException.LoadFailure -> {
                            showAppMessageDialog(CalendarAppMessage.DiaryInfoLoadFailure)
                        }
                        is ExistingDiaryDateListLoadException.Unknown -> {
                            showUnexpectedAppMessageDialog(it.exception)
                        }
                    }
                }
            }
        }.map {
            when (it) {
                is UseCaseResult.Success -> it.value
                is UseCaseResult.Failure -> emptyList()
            }.toSet()
        }.distinctUntilChanged().onEach {
            updateExistingDiaryDates(it)
        }.launchIn(viewModelScope)
    }

    /** 選択された日付の変更を監視し、対応する日記の準備を行う。 */
    private fun collectSelectedDate() {
        viewModelScope.launch {
            uiState.distinctUntilChanged{ old, new ->
                old.selectedDate == new.selectedDate
            }.map { it.selectedDate }.collectLatest {
                withUnexpectedErrorHandler {
                    prepareDiary(it)
                }
            }
        }
    }

    /** 天気2の表示/非表示状態の変更を監視し、UIに反映させる。 */
    private fun collectWeather2Visible() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.weather1 == new.weather1 && old.weather2 == new.weather2
        }.map {
            diaryUiStateHelper.isWeather2Visible(it.weather1, it.weather2)
        }.distinctUntilChanged().onEach { isWeather2Visible ->
            updateIsWeather2Visible(isWeather2Visible)
        }.launchIn(viewModelScope)
    }

    /** 表示されている日記項目数の変更を監視し、UIに反映させる。 */
    private fun collectNumVisibleDiaryItems() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.itemTitles == new.itemTitles
        }.map {
            diaryUiStateHelper.calculateNumVisibleDiaryItems(it.itemTitles)
        }.distinctUntilChanged().onEach { numVisibleDiaryItems ->
            updateNumVisibleDiaryItems(numVisibleDiaryItems)
        }.launchIn(viewModelScope)
    }

    /** 添付画像のファイルパスの変更を監視し、UIに反映させる。 */
    private fun collectImageFilePath() {
        diaryFlow.distinctUntilChanged{ old, new ->
            old.imageFileName == new.imageFileName
        }.map {
            diaryUiStateHelper.buildImageFilePath(it.imageFileName)
        }.catchUnexpectedError(
            FilePathUi.Unavailable
        ).distinctUntilChanged().onEach { path ->
            updateDiaryImageFilePath(path)
        }.launchIn(viewModelScope)
    }
    //endregion

    //region UI Event Handlers
    override fun onBackPressed() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            navigatePreviousScreen()
        }
    }

    /**
     * カレンダーの日付セルがクリックされた時に呼び出される事を想定。
     * 選択された日付を更新する。
     * @param date クリックされた日付
     */
    internal fun onCalendarDayClick(date: LocalDate) {
        updateSelectedDate(date)
    }

    /**
     * 日記編集ボタンがクリックされた時に呼び出される事を想定。
     * 日記編集画面へ遷移する。
     * */
    fun onDiaryEditButtonClick() {
        if (!isReadyForOperation) return

        val diaryLoadState = currentUiState.diaryLoadState
        var id: String?
        var date: LocalDate
        when (diaryLoadState) {
            is LoadState.Success -> {
                val diary = diaryLoadState.data
                id = diary.id
                date = diary.date
            }
            LoadState.Empty,
            is LoadState.Error -> {
                id = null
                date = currentUiState.selectedDate
            }

            LoadState.Idle,
            LoadState.Loading -> return
        }

        launchWithUnexpectedErrorHandler {
            navigateDiaryEditScreen(id, date)
        }
    }

    /**
     * カレンダー画面が表示されている状態で、再度ボトムナビゲーションの同タブが選択された時に呼び出される事を想定。
     * カレンダーを今日の日付へフォーカスする。
     * */
    internal fun onBottomNavigationItemReselect() {
        if (!isReadyForOperation) return

        val selectedDate = currentUiState.selectedDate
        launchWithUnexpectedErrorHandler {
            foucusOnToday(selectedDate)
        }
    }

    /**
     * 日記表示・編集画面から戻ってきた時に呼び出される事を想定。
     * 選択された日付を更新する。
     * @param date 日記表示・編集画面の表示されていた日記の日付
     */
    internal fun onDiaryShowFragmentResultReceived(date: LocalDate) {
        updateSelectedDate(date)
    }
    //endregion

    //region Business Logic
    /**
     * 指定された日付の表示準備を行う。カレンダーのスクロールと日記の読み込みを開始する。
     * @param date 準備対象の日付
     */
    private suspend fun prepareDiary(date: LocalDate) {
        if (suppressNextScroll) {
            updateSuppressNextScroll(false)
        } else {
            emitUiEvent(CalendarUiEvent.ScrollCalendar(date))
        }

        val exists = existsSavedDiary(date)
        if (exists) {
            loadSavedDiary(date)
        } else {
            updateToNoDiaryState()
        }
    }

    /**
     * 指定された日付に保存されている日記を読み込む。
     * @param date 読み込み対象の日付
     */
    private suspend fun loadSavedDiary(date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateToDiaryLoadingState()
        when (val result = loadDiaryByDateUseCase(date)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                val diary = result.value.toUiModel()
                updateToDiaryLoadSuccessState(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateToDiaryLoadErrorState()
            }
        }
    }

    /**
     * 指定された日付に日記が存在するかどうかを確認する。
     * @param date 確認対象の日付
     * @return 日記が存在する場合はtrue
     */
    private suspend fun existsSavedDiary(date: LocalDate): Boolean {
        when (val result = doesDiaryExistUseCase(date)) {
            is UseCaseResult.Success -> return result.value
            is UseCaseResult.Failure -> {
                when (result.exception) {
                    is DiaryExistenceCheckException.CheckFailure -> {
                        showAppMessageDialog(CalendarAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryExistenceCheckException.Unknown -> {
                        showUnexpectedAppMessageDialog(result.exception)
                    }
                }
                return false
            }
        }
    }

    /**
     * 今日の日付がカレンダーに表示されるようにスクロールし、今日の日付を選択する。
     * 既に今日が選択されている場合は、スクロールのみを行う。
     * @param selectedDate 現在選択されている日付。
     */
    private suspend fun foucusOnToday(selectedDate: LocalDate) {
        val today = LocalDate.now()
        emitUiEvent(
            CalendarUiEvent.SmoothScrollCalendar(today)
        )
        if (selectedDate == today) return

        updateSuppressNextScroll(true) // MEMO:日記読込準備時のスクロールを無効化
        updateSelectedDate(today)
    }

    /**
     * 前の画面へ遷移する（イベント発行）。
     */
    private suspend fun navigatePreviousScreen() {
        emitNavigationEvent(
            NavigationEvent.Back(
                NavigationEvent.Policy.Single,
                null
            )
        )
    }

    /**
     * 日記編集画面へ遷移する（イベント発行）。
     *
     * @param id 編集対象の日記のID。nullの場合は新規作成。
     * @param date 編集対象の日記の日付。
     */
    private suspend fun navigateDiaryEditScreen(id: String?, date: LocalDate) {
        emitNavigationEvent(
            NavigationEvent.To(
                CalendarNavDestination.DiaryEditScreen(id, date),
                NavigationEvent.Policy.Single
            )
        )
    }

    /**
     * アプリケーションメッセージダイアログを表示する（イベント発行）。
     * @param appMessage 表示するメッセージ。
     */
    private suspend fun showAppMessageDialog(appMessage: CalendarAppMessage) {
        emitNavigationEvent(
            NavigationEvent.To(
                CalendarNavDestination.AppMessageDialog(appMessage),
                NavigationEvent.Policy.Retry
            )
        )
    }

    override suspend fun showUnexpectedAppMessageDialog(e: Exception) {
        showAppMessageDialog(CalendarAppMessage.Unexpected(e))
    }
    //endregion

    //region UI State Update
    /**
     * 週の開始曜日を更新する。
     * @param dayOfWeek 新しい週の開始曜日
     */
    private fun updateCalendarStartDayOfWeek(dayOfWeek: DayOfWeek) {
        updateUiState { it.copy(calendarStartDayOfWeek = dayOfWeek) }
    }

    /**
     * 日記が存在する日付のセットを更新する。
     *
     * @param dates 日記が存在する日付のセット
     */
    private fun updateExistingDiaryDates(dates: Set<LocalDate>) {
        updateUiState { it.copy(existingDiaryDates = dates) }
    }

    /**
     * 選択された日付を更新する。
     * @param date 新しく選択された日付
     */
    private fun updateSelectedDate(date: LocalDate) {
        // MEMO:selectedDateと同日付を選択した時、previousSelectedDateと同値となり、
        //      次に他の日付を選択した時にpreviousSelectedDateのcollectedが起動しなくなる。
        //      下記条件で対策。
        if (date == currentUiState.selectedDate) return

        updateUiState {
            it.copy(
                selectedDate = date,
                previousSelectedDate = it.selectedDate
            )
        }
    }

    /**
     * 天気2の表示状態を更新する。
     * @param isVisible 表示する場合はtrue
     */
    private fun updateIsWeather2Visible(isVisible: Boolean) {
        updateUiState { it.copy(isWeather2Visible = isVisible) }
    }

    /**
     * 表示されている日記項目数を更新する。
     * @param count 表示する日記項目の数
     */
    private fun updateNumVisibleDiaryItems(count: Int) {
        updateUiState { it.copy(numVisibleDiaryItems = count) }
    }

    /**
     * 添付画像のファイルパスを更新する。
     * @param path 新しいファイルパス
     */
    private fun updateDiaryImageFilePath(path: FilePathUi?) {
        updateUiState { it.copy(diaryImageFilePath = path) }
    }

    /** UIを日記読み込み中の状態に更新する。 */
    private fun updateToDiaryLoadingState() {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Loading,
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    /**
     * UIを日記読み込み成功の状態に更新する。
     * @param diary 読み込んだ日記データ
     */
    private fun updateToDiaryLoadSuccessState(diary: DiaryUi) {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Success(diary),
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    /** UIを日記読み込み失敗の状態に更新する。 */
    private fun updateToDiaryLoadErrorState() {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Error,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    /** UIを日記なしの状態に更新する。 */
    private fun updateToNoDiaryState() {
        updateUiState {
            it.copy(
                diaryLoadState = LoadState.Empty,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }
    //endregion

    //region Internal State Update
    /**
     * 次回スクロールを抑制するかどうかのフラグを更新する。
     * @param shouldScroll 抑制する場合はtrue
     */
    private fun updateSuppressNextScroll(shouldScroll: Boolean) {
        suppressNextScroll = shouldScroll
    }
    //endregion

    private companion object {
        /** SavedStateHandleにUI状態を保存するためのキー。 */
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
