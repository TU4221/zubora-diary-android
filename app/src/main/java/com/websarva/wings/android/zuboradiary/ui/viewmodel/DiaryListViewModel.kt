package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.BuildConfig
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.diary.SavedDiaryDateRange
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.domain.model.diary.list.diary.DiaryYearMonthList
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryListStartYearMonthPickerDateRangeUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.RefreshDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListAdditionLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListNewLoadException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListRefreshException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryListStartYearMonthPickerDateRangeLoadException
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryListAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryListUiEvent
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListItemContainerUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryListUi
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.CancellationException
import javax.inject.Inject

/**
 * 日記リスト画面のUIロジックと状態([DiaryListUiState])管理を担うViewModel。
 *
 * 以下の責務を持つ:
 * - 条件に基づいた日記リストの新規読み込み、追加読み込み、および更新
 * - ユーザー操作（アイテムクリック、削除、スクロールなど）に応じたイベント処理
 * - 絞り込み条件（年月）の選択や、日記表示・編集画面への遷移イベントの発行
 * - [SavedStateHandle]を利用して、プロセスの再生成後もUI状態を復元とリストの再読み込み
 */
@HiltViewModel
class DiaryListViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val loadNewDiaryListUseCase: LoadNewDiaryListUseCase,
    private val loadAdditionDiaryListUseCase: LoadAdditionDiaryListUseCase,
    private val refreshDiaryListUseCase: RefreshDiaryListUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val loadDiaryListStartYearMonthPickerDateRangeUseCase: LoadDiaryListStartYearMonthPickerDateRangeUseCase,
    private val buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) : BaseFragmentViewModel<DiaryListUiState, DiaryListUiEvent, DiaryListAppMessage>(
    handle.get<DiaryListUiState>(SAVED_STATE_UI_KEY)?.let {
        DiaryListUiState.fromSavedState(it)
    } ?: DiaryListUiState()
) {

    //region Properties
    /** 日記リストの読み込み処理を管理するための[Job]。多重実行を防ぐために使用する。 */
    private var diaryListLoadJob: Job? = null

    /** プロセス復帰（Process Deathからの復元）直後であるかを示すフラグ。 */
    private var isRestoringFromProcessDeath: Boolean = false

    /** 画面が非表示から復帰した際に、リストをリフレッシュする必要があるかを示すフラグ。 */
    private var needsRefreshDiaryList = false // MEMO:画面遷移、回転時の更新フラグ

    /** スクロールによる追加読み込みが現在実行中であるかを示すStateFlow。 */
    private val _isLoadingOnScrolled = MutableStateFlow(false)
    internal val isLoadingOnScrolled = _isLoadingOnScrolled.asStateFlow()

    /** 削除処理が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null
    //endregion

    //region Initialization
    init {
        checkForRestoration()
        collectUiStates()
    }

    /** プロセス復帰からのリストアかどうかを確認し、フラグを更新する。 */
    private fun checkForRestoration() {
        updateIsRestoringFromProcessDeath(
            handle.contains(SAVED_STATE_UI_KEY)
        )
    }

    /** UI状態の監視を開始する。 */
    private fun collectUiStates() {
        collectUiState()
        collectDiaryListSortConditionDate()
    }

    /** UI状態を[SavedStateHandle]に保存する。 */
    private fun collectUiState() {
        uiState.onEach { 
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    /** 日記リストのソート条件日付の変更を監視し、リストの再読み込みをトリガーする。 */
    private fun collectDiaryListSortConditionDate() {
        viewModelScope.launch {
            uiState.distinctUntilChanged { oldState, newState ->
                oldState.sortConditionDate == newState.sortConditionDate
            }.map { 
                Pair(it.diaryList, it.sortConditionDate)
            }.collectLatest { (diaryList, sortConditionDate) ->
                withUnexpectedErrorHandler {
                    if (isRestoringFromProcessDeath) {
                        updateIsRestoringFromProcessDeath(false)
                        refreshDiaryList(diaryList, sortConditionDate)
                        return@withUnexpectedErrorHandler
                    }

                    loadNewDiaryList(
                        diaryList,
                        sortConditionDate
                    )
                }
            }
        }
    }
    //endregion

    //region UI Event Handlers - Observation
    /** 
     * UIが準備完了した時に、`Fragment`から呼び出される事を想定。
     * 必要に応じてリストのリフレッシュを行う。
     * */
    internal fun onUiReady() {
        if (!needsRefreshDiaryList) return
        updateNeedsRefreshDiaryList(false)
        if (!isReadyForOperation) return

        val currentList = currentUiState.diaryList
        val sortConditionDate = currentUiState.sortConditionDate
        cancelPreviousLoadJob()
        diaryListLoadJob =
            launchWithUnexpectedErrorHandler {
                refreshDiaryList(currentList, sortConditionDate)
            }
    }

    /** 
     * UIが非表示になる時に、`Fragment`から呼び出される事を想定。
     * 次回表示時にリストをリフレッシュするためのフラグを立てる。 
     * */
    internal fun onUiGone() {
        updateNeedsRefreshDiaryList(true)
    }
    //endregion

    //region UI Event Handlers - Action
    override fun onBackPressed() {
        // MEMO:DiaListFragmentはスタートフラグメントに該当するため、
        //      BaseFragmentでOnBackPressedCallbackを登録せずにNavigation機能のデフォルト戻る機能を使用する。
        //      そのため、本メソッドは呼び出されない。
        launchWithUnexpectedErrorHandler {
            emitNavigatePreviousFragmentEvent()
        }
    }

    /**
     * ワード検索メニューがクリックされた時に呼び出される事を想定。
     * ワード検索画面へ遷移するイベントを発行する。
     * */
    internal fun onWordSearchMenuClick() {
        launchWithUnexpectedErrorHandler {
            emitUiEvent(DiaryListUiEvent.NavigateWordSearchFragment)
        }
    }

    /**
     * ナビゲーションアイコン(カレンダーアイコン)がクリックされた時に呼び出される事を想定。
     * 開始年月選択ダイアログへ遷移するイベントを発行する。
     * */
    fun onNavigationIconClick() {
        launchWithUnexpectedErrorHandler {
            val dateRange = loadSavedDiaryDateRange()
            val newestDiaryDate = dateRange.newestDiaryDate
            val oldestDiaryDate = dateRange.oldestDiaryDate
            val newestYear = Year.of(newestDiaryDate.year)
            val oldestYear = Year.of(oldestDiaryDate.year)
            emitUiEvent(
                DiaryListUiEvent.NavigateStartYearMonthPickerDialog(newestYear, oldestYear)
            )
        }
    }

    /**
     * 日記リストのアイテムがクリックされた時に呼び出される事を想定。
     * 日記表示画面へ遷移するイベントを発行する。
     * */
    internal fun onDiaryListItemClick(item: DiaryListItemContainerUi.Standard) {
        val id = item.id
        val date = item.date
        launchWithUnexpectedErrorHandler {
            emitUiEvent(DiaryListUiEvent.NavigateDiaryShowFragment(id, date))
        }
    }

    /**
     * 日記リストアイテムの削除ボタンがクリックされた時に呼び出される事を想定。
     * 日記リストアイテム削除ダイアログへ遷移するイベントを発行する。
     * */
    internal fun onDiaryListItemDeleteButtonClick(item: DiaryListItemContainerUi.Standard) {
        if (!isReadyForOperation) return

        val id = item.id
        val date = item.date
        val currentList = currentUiState.diaryList
        val sortConditionDate = currentUiState.sortConditionDate
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(
                DiaryId(id),
                currentList,
                sortConditionDate
            )
            emitUiEvent(
                DiaryListUiEvent.NavigateDiaryDeleteDialog(date)
            )
        }
    }

    /**
     * 日記編集ボタン(FAB)がクリックされた時に呼び出される事を想定。
     * 日記編集画面へ遷移するイベントを発行する。
     * */
    fun onDiaryEditButtonClick() {
        launchWithUnexpectedErrorHandler {
            val today = LocalDate.now()
            emitUiEvent(DiaryListUiEvent.NavigateDiaryEditFragment(date = today))
        }
    }

    /**
     * 日記リストが末尾までスクロールされた時に呼び出される事を想定。
     * 日記リストの追加読み込みを開始する。
     * */
    internal fun onDiaryListEndScrolled() {
        if (_isLoadingOnScrolled.value) return
        updateIsLoadingOnScrolled(true)

        val currentList = currentUiState.diaryList
        val sortConditionDate = currentUiState.sortConditionDate
        cancelPreviousLoadJob()
        diaryListLoadJob =
            launchWithUnexpectedErrorHandler {
                loadAdditionDiaryList(currentList, sortConditionDate)
            }
    }

    /**
     * 日記リストのRecyclerViewのアダプター更新が完了した時に呼び出される事を想定。
     * 追加読み込み中フラグを解除する。
     * */
    internal fun onDiaryListUpdateCompleted() {
        updateIsLoadingOnScrolled(false)
    }
    //endregion

    //region UI Event Handlers - Results
    /**
     * 開始年月選択ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * ソート条件日付を更新する。
     */
    internal fun onDatePickerDialogPositiveResultReceived(yearMonth: YearMonth) {
        val sortConditionDate =
            yearMonth.atDay(1).with(TemporalAdjusters.lastDayOfMonth())
        updateSortConditionDate(sortConditionDate)
    }

    /**
     * 日記削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 日記の削除を実行する。
     */
    internal fun onDiaryDeleteDialogPositiveResultReceived() {
        val parameters = checkNotNull(pendingDiaryDeleteParameters)
        clearPendingDiaryDeleteParameters()
        launchWithUnexpectedErrorHandler {
            deleteDiary(
                parameters.id,
                parameters.currentList,
                parameters.sortConditionDate
            )
        }
    }

    /**
     * 日記削除確認ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 日記削除パラメータ([pendingDiaryDeleteParameters])をクリアする。
     */
    internal fun onDiaryDeleteDialogNegativeResultReceived() {
        clearPendingDiaryDeleteParameters()
    }
    //endregion

    //region Business Logic
    /** 実行中の日記リスト読み込み処理があればキャンセルする。 */
    private fun cancelPreviousLoadJob() {
        val job = diaryListLoadJob ?: return
        if (!job.isCompleted) job.cancel()
    }

    /**
     * 新しい日記リストを読み込む。
     * @param currentList 現在の日記リスト
     * @param sortConditionDate 絞り込み条件の日付
     */
    private suspend fun loadNewDiaryList(
        currentList: DiaryListUi<DiaryListItemContainerUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        executeLoadDiaryList(
            { updateToDiaryListNewLoadState() },
            currentList,
            { _ ->
                loadNewDiaryListUseCase(sortConditionDate)
            },
            { exception ->
                when (exception) {
                    is DiaryListNewLoadException.LoadFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
                    }
                    is DiaryListNewLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    /**
     * 追加の日記リストを読み込む。
     * @param currentList 現在の日記リスト
     * @param sortConditionDate 絞り込み条件の日付
     */
    private suspend fun loadAdditionDiaryList(
        currentList: DiaryListUi<DiaryListItemContainerUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        executeLoadDiaryList(
            { updateToDiaryListAdditionLoadState() },
            currentList,
            { lambdaCurrentList ->
                require(lambdaCurrentList.isNotEmpty)

                loadAdditionDiaryListUseCase(
                    lambdaCurrentList,
                    sortConditionDate
                )
            },
            { exception ->
                when (exception) {
                    is DiaryListAdditionLoadException.LoadFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
                    }
                    is DiaryListAdditionLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    /**
     * 現在の日記リストをリフレッシュする。
     * @param currentList 現在の日記リスト
     * @param sortConditionDate 絞り込み条件の日付
     */
    private suspend fun refreshDiaryList(
        currentList: DiaryListUi<DiaryListItemContainerUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        executeLoadDiaryList(
            { updateToDiaryListRefreshState() },
            currentList,
            { lambdaCurrentList ->
                refreshDiaryListUseCase(lambdaCurrentList, sortConditionDate)
            },
            { exception ->
                when (exception) {
                    is DiaryListRefreshException.RefreshFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryListLoadFailure)
                    }
                    is DiaryListRefreshException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    /**
     * 日記リストの読み込み処理を共通のロジックで実行する。
     * @param updateToLoadingUiState 読み込み開始時のUI状態更新処理
     * @param currentList 現在の日記リスト
     * @param executeLoad 実際の読み込みを行うUseCaseの実行処理
     * @param emitAppMessageOnFailure 読み込み失敗時のメッセージ発行処理
     */
    private suspend fun <E : UseCaseException> executeLoadDiaryList(
        updateToLoadingUiState: suspend () -> Unit,
        currentList: DiaryListUi<DiaryListItemContainerUi.Standard>,
        executeLoad: suspend (DiaryYearMonthList<DiaryDayListItem.Standard>)
        -> UseCaseResult<DiaryYearMonthList<DiaryDayListItem.Standard>, E>,
        emitAppMessageOnFailure: suspend (E) -> Unit
    ) {

        val logMsg = "日記リスト読込"
        Log.i(logTag, "${logMsg}_開始")

        updateToLoadingUiState()
        try {
            when (val result = executeLoad(currentList.toDomainModel())) {
                is UseCaseResult.Success -> {
                    val updateDiaryList = mapDiaryListUiModel(result.value)
                    updateToDiaryListLoadCompletedState(updateDiaryList)
                    Log.i(logTag, "${logMsg}_完了")
                }
                is UseCaseResult.Failure -> {
                    Log.e(logTag, "${logMsg}_失敗", result.exception)
                    updateToIdleState()
                    emitAppMessageOnFailure(result.exception)
                }
            }
        } catch (e: CancellationException) {
            Log.i(logTag, "${logMsg}_キャンセル", e)
            updateToIdleState()
            throw e // 再スローしてコルーチン処理を中断させる
        }
    }

    /**
     * ドメインモデルの日記リストをUIモデルにマッピングする。
     * @param list 変換元のドメインモデルのリスト ([DiaryYearMonthList])
     * @return UI層で表示可能な日記リスト ([DiaryListUi])
     */
    private suspend fun mapDiaryListUiModel(
        list: DiaryYearMonthList<DiaryDayListItem.Standard>
    ): DiaryListUi<DiaryListItemContainerUi.Standard>{
        return list.toUiModel { fileName: DiaryImageFileName? ->
            fileName?.let {
                try {
                    when (val buildResult = buildDiaryImageFilePathUseCase(fileName)) {
                        is UseCaseResult.Success -> FilePathUi.Available(buildResult.value)
                        is UseCaseResult.Failure -> FilePathUi.Unavailable
                    }
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        throw e
                    } else {
                        FilePathUi.Unavailable
                    }
                }

            }
        }
    }

    /**
     * 日記を削除し、リストをリフレッシュする。
     * @param id 削除対象の日記ID
     * @param currentList 現在の日記リスト
     * @param sortConditionDate 絞り込み条件の日付
     */
    private suspend fun deleteDiary(
        id: DiaryId,
        currentList: DiaryListUi<DiaryListItemContainerUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateToProcessingState()
        when (val result = deleteDiaryUseCase(id)) {
            is UseCaseResult.Success -> {
                refreshDiaryList(currentList, sortConditionDate)
                Log.i(logTag, "${logMsg}_完了")
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateToIdleState()
                when (result.exception) {
                    is DiaryDeleteException.DiaryDataDeleteFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryDeleteFailure)
                    }
                    is DiaryDeleteException.ImageFileDeleteFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryImageDeleteFailure)
                    }
                    is DiaryDeleteException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    /**
     * 保存されている日記の最も新しい日付と最も古い日付の範囲を取得する。
     * @return 日付範囲を保持する[SavedDiaryDateRange]
     */
    private suspend fun loadSavedDiaryDateRange(): SavedDiaryDateRange {
        updateToProcessingState()
        val dateRange = when (val result = loadDiaryListStartYearMonthPickerDateRangeUseCase()) {
            is UseCaseResult.Success -> result.value
            is UseCaseResult.Failure -> {
                when (val exception = result.exception) {
                    is DiaryListStartYearMonthPickerDateRangeLoadException.DiaryInfoLoadFailure -> {
                        emitAppMessageEvent(DiaryListAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryListStartYearMonthPickerDateRangeLoadException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
                result.exception.fallbackDateRange
            }
        }
        updateToIdleState()
        return dateRange
    }
    //endregion

    //region UI State Update
    /**
     * 日記リストの読み込みソート条件日付を更新する。
     * @param date 新しいソート条件日付
     */
    private fun updateSortConditionDate(date: LocalDate?) {
        updateUiState { it.copy(sortConditionDate = date) }
    }

    /** UIをアイドル状態（操作可能）に更新する。 */
    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }

    /** UIを処理中の状態（操作不可）に更新する。 */
    private fun updateToProcessingState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    /**
     * UIを新規リスト読み込み中の状態に更新する。
     */
    private suspend fun updateToDiaryListNewLoadState() {
        val list =
            mapDiaryListUiModel(DiaryYearMonthList.initialLoadingDiaryList())
        updateUiState {
            it.copy(
                diaryList = list,
                hasNoDiaries = false,
                isProcessing = false,
                isInputDisabled = true
            )
        }
    }

    /** UIを追加リスト読み込み中の状態に更新する。 */
    private fun updateToDiaryListAdditionLoadState() {
        updateUiState { it.copy(isInputDisabled = true) }
    }

    /** UIをリスト更新中の状態に更新する。 */
    private fun updateToDiaryListRefreshState() {
        updateUiState { it.copy(isInputDisabled = true) }
    }

    /**
     * UIをリスト読み込み完了の状態に更新する。
     * @param list 更新後の日記リスト
     */
    private fun updateToDiaryListLoadCompletedState(
        list: DiaryListUi<DiaryListItemContainerUi.Standard>
    ) {
        updateUiState {
            it.copy(
                diaryList = list,
                hasNoDiaries = list.isEmpty,
                isProcessing = false,
                isInputDisabled = false,
                isRefreshing = false
            )
        }
    }
    //endregion

    //region Internal State Update
    /**
     * プロセス復帰からのリストア中かどうかのフラグを更新する。
     * @param isRestoring リストア中の場合はtrue
     */
    private fun updateIsRestoringFromProcessDeath(isRestoring: Boolean) {
        isRestoringFromProcessDeath = isRestoring
    }

    /**
     * リストをリフレッシュする必要があるかどうかのフラグを更新する。
     * @param needsRefresh リフレッシュが必要な場合はtrue
     */
    private fun updateNeedsRefreshDiaryList(needsRefresh: Boolean) {
        needsRefreshDiaryList = needsRefresh
    }

    /**
     * スクロールによる追加読み込み中かどうかの状態を更新する。
     * @param isLoading 読み込み中の場合はtrue
     */
    private fun updateIsLoadingOnScrolled(isLoading: Boolean) {
        _isLoadingOnScrolled.value = isLoading
    }
    //endregion

    //region Pending Diary Delete Parameters
    /**
     * 保留中の日記削除パラメータを更新する。
     * @param id 削除対象の日記ID
     * @param currentList 現在の日記リスト
     * @param sortConditionDate 現在のソート条件日付
     */
    private fun updatePendingDiaryDeleteParameters(
        id: DiaryId,
        currentList: DiaryListUi<DiaryListItemContainerUi.Standard>,
        sortConditionDate: LocalDate?
    ) {
        pendingDiaryDeleteParameters = DiaryDeleteParameters(id, currentList, sortConditionDate)
    }

    /** 保留中の日記削除パラメータをクリアする。 */
    private fun clearPendingDiaryDeleteParameters() {
        pendingDiaryDeleteParameters = null
    }

    /**
     * 日記削除処理に必要なパラメータを保持するデータクラス。
     * @property id 削除対象の日記ID
     * @property currentList 現在の日記リスト
     * @property sortConditionDate 現在のソート条件日付
     */
    private data class DiaryDeleteParameters(
        val id: DiaryId,
        val currentList: DiaryListUi<DiaryListItemContainerUi.Standard>,
        val sortConditionDate: LocalDate?
    )
    //endregion

    private companion object {
        /** SavedStateHandleにUI状態を保存するためのキー。 */
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
