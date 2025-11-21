package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.exception.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.ui.model.diary.ConditionUi
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistoryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySaveConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ClearDiaryImageCacheFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CacheDiaryImageUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryExistenceCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageCacheException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryImageCacheFileClearException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByDateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByIdException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadConfirmationCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiarySaveException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryUpdateConfirmationCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.CheckWeatherInfoFetchEnabledUseCase
import com.websarva.wings.android.zuboradiary.ui.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryEditAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryEditUiEvent
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionHistoryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryEditUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryUiStateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.collections.map

/**
 * 日記編集画面のUIロジックと状態([DiaryEditUiState])管理を担うViewModel。
 *
 * 以下の責務を持つ:
 * - 新規または既存の日記データの読み込み
 * - ユーザーによる入力（タイトル、天気、体調、各項目など）をUI状態へ反映する
 * - 天気情報取得や画像キャッシュなどの非同期処理の実行と、それに伴うUI状態の更新
 * - 編集内容の保存、更新、または削除処理の実行
 * - ユーザーの操作や処理の結果に応じて、確認ダイアログの表示や画面遷移などのUIイベントを発行する
 * - [SavedStateHandle]を利用して、プロセスの再生成後もUI状態を復元する
 */
@HiltViewModel
class DiaryEditViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val diaryUiStateHelper: DiaryUiStateHelper,
    private val shouldRequestExitWithoutDiarySaveConfirmationUseCase: ShouldRequestExitWithoutDiarySaveConfirmationUseCase,
    private val shouldRequestDiaryLoadConfirmationUseCase: ShouldRequestDiaryLoadConfirmationUseCase,
    private val shouldRequestDiaryUpdateConfirmationUseCase: ShouldRequestDiaryUpdateConfirmationUseCase,
    private val shouldRequestWeatherInfoConfirmationUseCase: ShouldRequestWeatherInfoConfirmationUseCase,
    private val loadDiaryByIdUseCase: LoadDiaryByIdUseCase,
    private val loadDiaryByDateUseCase: LoadDiaryByDateUseCase,
    private val saveDiaryUseCase: SaveDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val checkWeatherInfoFetchEnabledUseCase: CheckWeatherInfoFetchEnabledUseCase,
    private val fetchWeatherInfoUseCase: FetchWeatherInfoUseCase,
    private val shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase,
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase,
    private val cacheDiaryImageUseCase: CacheDiaryImageUseCase,
    private val clearDiaryImageCacheFileUseCase: ClearDiaryImageCacheFileUseCase
) : BaseFragmentViewModel<DiaryEditUiState, DiaryEditUiEvent, DiaryEditAppMessage>(
    handle.get<DiaryEditUiState>(SAVED_STATE_UI_KEY)?.let {
        DiaryEditUiState.fromSavedState(it)
    } ?: DiaryEditUiState(editingDiary = Diary.generate().toUiModel())
) {


    //region Properties
    /** このViewModelが操作を受け入れ可能な状態かを示す。 */
    override val isReadyForOperation
        get() = !currentUiState.isInputDisabled
                && currentUiState.originalDiaryLoadState is LoadState.Success

    /** 編集元の日記データへのアクセスを提供する。 */
    private val originalDiary
        get() = (currentUiState.originalDiaryLoadState as LoadState.Success).data

    /** 編集中の日記データへのアクセスを提供する。 */
    private val editingDiaryFlow =
        uiState.distinctUntilChanged { old, new ->
            old.editingDiary == new.editingDiary
        }.map { it.editingDiary }

    // キャッシュパラメータ
    /** 日記の読み込みが保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingDiaryLoadParameters: DiaryLoadParameters? = null

    /** 日記の更新が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingDiaryUpdateParameters: DiaryUpdateParameters? = null

    /** 日記の削除が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null

    /** 日記の日付変更が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingDiaryDateUpdateParameters: DiaryDateUpdateParameters? = null

    /** 日記項目の削除が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingDiaryItemDeleteParameters: DiaryItemDeleteParameters? = null

    /** 日記画像の更新が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingDiaryImageUpdateParameters: DiaryImageUpdateParameters? = null

    /** 天気情報の取得が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingWeatherInfoFetchParameters: WeatherInfoFetchParameters? = null

    /** 前の画面へ戻るのが保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingPreviousNavigationParameters: PreviousNavigationParameters? = null
    //endregion

    //region Initialization
    init {
        initializeDiaryData()
        collectUiStates()
    }

    /**
     * [SavedStateHandle]に保存されたデータがない場合のみ、引数から日記IDと日付を取得し、日記データの初期化を開始する。
     *
     */
    private fun initializeDiaryData() {
        // MEMO:下記条件はアプリ設定変更時のアプリ再起動時の不要初期化対策
        if (handle.contains(SAVED_STATE_UI_KEY)) return
        val id = handle.get<String>(ARGUMENT_DIARY_ID_KEY)?.let { DiaryId(it) }
        val date =
            handle.get<LocalDate>(ARGUMENT_DIARY_DATE_KEY) ?: throw IllegalArgumentException()
        launchWithUnexpectedErrorHandler {
            prepareDiaryEntry(
                id,
                date
            )
        }
    }

    /** UI状態の監視を開始する。 */
    private fun collectUiStates() {
        collectUiState()
        collectWeather2Options()
        collectWeather2Enabled()
        collectNumVisibleDiaryItems()
        collectDiaryItemAdditionEnabled()
        collectImageFilePath()
    }

    /** UI状態を[SavedStateHandle]に保存する。 */
    private fun collectUiState() {
        uiState.onEach {
            Log.d(logTag, it.toString())
            handle[SAVED_STATE_UI_KEY] = it
        }.launchIn(viewModelScope)
    }

    /** 天気2の選択肢の変更を監視し、UIに反映させる。 */
    private fun collectWeather2Options() {
        uiState.distinctUntilChanged { old, new ->
            old.editingDiary.weather1 == new.editingDiary.weather1
        }.map { state ->
            WeatherUi.entries.filter { weather ->
                weather != state.editingDiary.weather1
            }
        }.distinctUntilChanged().onEach { options ->
            updateWeather2Options(options)
        }.launchIn(viewModelScope)
    }

    /** 天気2の選択可否の変更を監視し、UIに反映させる。 */
    private fun collectWeather2Enabled() {
        editingDiaryFlow.distinctUntilChanged{ old, new ->
            old.weather1 == new.weather1 && old.weather2 == new.weather2
        }.map { editingDiary ->
            when (editingDiary.weather1) {
                WeatherUi.UNKNOWN -> false
                else -> {
                    editingDiary.weather1 != editingDiary.weather2
                }
            }
        }.distinctUntilChanged().onEach { isEnabled ->
            if (isEnabled) {
                updateToWeather2EnabledState()
            } else {
                updateToWeather2DisabledState()
            }
        }.launchIn(viewModelScope)
    }

    /** 表示する日記項目数の変更を監視し、UIに反映させる。 */
    private fun collectNumVisibleDiaryItems() {
        editingDiaryFlow.distinctUntilChanged{ old, new ->
            old.itemTitles == new.itemTitles
        }.map {
            diaryUiStateHelper.calculateNumVisibleDiaryItems(it.itemTitles)
        }.distinctUntilChanged().onEach { numVisibleDiaryItems ->
            updateNumVisibleItems(numVisibleDiaryItems)
            emitUiEvent(
                DiaryEditUiEvent.UpdateDiaryItemLayout(numVisibleDiaryItems)
            )
        }.launchIn(viewModelScope)
    }

    /** 日記項目追加可否の変更を監視し、UIに反映させる。 */
    private fun collectDiaryItemAdditionEnabled() {
        uiState.distinctUntilChanged { old, new ->
            old.isInputDisabled == new.isInputDisabled
                    && old.numVisibleDiaryItems == new.numVisibleDiaryItems
        }.map {
            !it.isInputDisabled && it.numVisibleDiaryItems < DiaryItemNumber.MAX_NUMBER
        }.distinctUntilChanged().onEach { isEnabled ->
            updateIsDiaryItemAdditionEnabled(isEnabled)
        }.launchIn(viewModelScope)
    }

    /** 添付画像のファイルパスの変更を監視し、UIに反映させる。 */
    private fun collectImageFilePath() {
        editingDiaryFlow.distinctUntilChanged{ old, new ->
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

    //region UI Event Handlers - Action
    override fun onBackPressed() {
        if (!isReadyForOperation) return

        val diary = currentUiState.editingDiary.toDomainModel()
        val originalDiary = originalDiary.toDomainModel()
        launchWithUnexpectedErrorHandler {
            handleBackNavigation(diary, originalDiary)
        }
    }

    /**
     * 保存メニューがクリックされた時に呼び出される事を想定。
     * 日記の保存処理を開始する。
     */
    internal fun onDiarySaveMenuClick() {
        if (!isReadyForOperation) return

        updateLog(LocalDateTime.now())
        val diary = currentUiState.editingDiary.toDomainModel()
        val diaryItemTitleSelectionHistoryList =
            currentUiState.diaryItemTitleSelectionHistories
                .values.filterNotNull().map { it.toDomainModel() }
        val originalDiary = originalDiary.toDomainModel()
        val isNewDiary = currentUiState.isNewDiary
        launchWithUnexpectedErrorHandler {
            requestDiaryUpdateConfirmation(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            )
        }
    }

    /**
     * 日記削除メニューがクリックされた時に呼び出される事を想定。
     * 日記の保存を開始する。
     * 日記削除ダイアログへ遷移するイベントを発行する。
     */
    internal fun onDiaryDeleteMenuClick() {
        if (!isReadyForOperation) return
        if (currentUiState.isNewDiary) return

        val originalDiaryId = DiaryId(originalDiary.id)
        val originalDiaryDate = originalDiary.date
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDeleteParameters(
                originalDiaryId,
                originalDiaryDate
            )
            emitUiEvent(
                DiaryEditUiEvent.NavigateDiaryDeleteDialog(originalDiaryDate)
            )
        }

    }

    /**
     * ナビゲーションアイコンがクリックされた時に呼び出される事を想定。
     * 前の画面へ戻る処理を実行する。
     * 編集途中の場合は編集途中確認ダイアログへ遷移するイベントを発行する。
     */
    fun onNavigationClick() {
        if (!isReadyForOperation) return

        val diary = currentUiState.editingDiary.toDomainModel()
        val originalDiary = originalDiary.toDomainModel()
        launchWithUnexpectedErrorHandler {
            handleBackNavigation(diary, originalDiary)
        }
    }

    /**
     * 日付入力欄がクリックされた時に呼び出される事を想定。
     * 日付選択ダイアログへ遷移するイベントを発行する。
     */
    fun onDateInputFieldClick() {
        if (!isReadyForOperation) return

        val date = currentUiState.editingDiary.date
        val originalDate = originalDiary.date
        val isNewDiary = currentUiState.isNewDiary
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryDateUpdateParameters(originalDate, isNewDiary)
            emitUiEvent(
                DiaryEditUiEvent.NavigateDatePickerDialog(date)
            )
        }
    }

    /**
     * 天気1の選択肢がクリックされた時に呼び出される事を想定。
     * 天気1を更新する。
     */
    fun onWeather1InputFieldItemClick(position: Int) {
        val selectWeather = currentUiState.weather1Options[position]
        updateWeather1(selectWeather)
    }

    /**
     * 天気2の選択肢がクリックされた時に呼び出される事を想定。
     * 天気2を更新する。
     */
    fun onWeather2InputFieldItemClick(position: Int) {
        val selectWeather = currentUiState.weather2Options[position]
        updateWeather2(selectWeather)
    }

    /**
     * 体調の選択肢がクリックされた時に呼び出される事を想定。
     * 体調を更新する。
     */
    fun onConditionInputFieldItemClick(position: Int) {
        val selectCondition = currentUiState.conditionOptions[position]
        updateCondition(selectCondition)
    }

    /**
     * 日記タイトル入力欄のテキストが変更された時に呼び出される事を想定。
     * 日記タイトルを更新する。
     * @param text 変更後のテキスト
     */
    fun onTitleTextChanged(text: CharSequence) {
        updateTitle(text.toString())
    }

    /**
     * 日記項目タイトル入力欄がクリックされた時に呼び出される事を想定。
     * 日記項目タイトル編集ダイアログへ遷移するイベントを発行する。
     * @param itemNumberInt 対象の項目番号
     */
    fun onItemTitleInputFieldClick(itemNumberInt: Int) {
        if (!isReadyForOperation) return

        val itemTitleId = null // MEMO:日記項目タイトルIDは受取用でここでは不要の為、nullとする。
        val itemTitle =
            currentUiState.editingDiary.itemTitles[itemNumberInt] ?: throw IllegalStateException()

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryEditUiEvent.NavigateDiaryItemTitleEditFragment(
                    DiaryItemTitleSelectionUi(
                        itemNumberInt,
                        itemTitleId,
                        itemTitle
                    )
                )
            )
        }
    }

    /**
     * 日記項目タイトル入力欄のテキストが変更された時に呼び出される事を想定。
     * 日記項目タイトルを更新する。
     * @param itemNumberInt 対象の項目番号
     * @param text 変更後のテキスト
     */
    fun onItemTitleTextChanged(itemNumberInt: Int, text: CharSequence) {
        updateItemTitle(
            itemNumberInt,
            text.toString()
        )
    }

    /**
     * 日記項目追加ボタンがクリックされた時に呼び出される事を想定。
     * 日記項目入力欄の追加処理を実行する。
     */
    fun onItemAdditionButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            addDiaryItem()
        }
    }

    /**
     * 日記項目コメント入力欄のテキストが変更された時に呼び出される事を想定。
     * 日記項目コメントを更新する。
     * @param itemNumberInt 対象の項目番号
     * @param text 変更後のテキスト
     */
    fun onItemCommentTextChanged(itemNumberInt: Int, text: CharSequence) {
        updateItemComment(
            itemNumberInt,
            text.toString()
        )
    }

    /**
     * 日記項目削除ボタンがクリックされた時に呼び出される事を想定。
     * 日記項目入力欄を削除するイベントを発行する。
     */
    fun onItemDeleteButtonClick(itemNumberInt: Int) {
        if (!isReadyForOperation) return

        val itemNumber = DiaryItemNumber(itemNumberInt)
        launchWithUnexpectedErrorHandler {
            updatePendingDiaryItemDeleteParameters(itemNumber)
            emitUiEvent(
                DiaryEditUiEvent.NavigateDiaryItemDeleteDialog(itemNumberInt)
            )
        }
    }

    /**
     * 添付画像削除ボタンがクリックされた時に呼び出される事を想定。
     * 添付画像を削除するイベントを発行する。
     */
    fun onAttachedImageDeleteButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            emitUiEvent(
                DiaryEditUiEvent.NavigateDiaryImageDeleteDialog
            )
        }
    }

    /**
     * 添付画像欄がクリックされた時に呼び出される事を想定。
     * 添付する画像を選択する画面へ遷移する処理を実行する。
     */
    fun onAttachedImageClick() {
        if (!isReadyForOperation) return

        val diaryId = currentUiState.editingDiary.id.let { DiaryId(it) }
        launchWithUnexpectedErrorHandler {
            selectImage(diaryId)
        }
    }

    /**
     * 日記項目を非表示(削除)にするアニメーションが完了した時に呼び出される事を想定。
     * 日記項目データを削除する。
     * @param itemNumberInt 対象の項目番号
     * */
    internal fun onDiaryItemInvisibleStateTransitionCompleted(itemNumberInt: Int) {
        val itemNumber = DiaryItemNumber(itemNumberInt)
        deleteItem(itemNumber)
    }

    /**
     * 日記項目を表示(追加)するアニメーションが完了した時に呼び出される事を想定。
     * UIをアイドル状態にする
     * */
    internal fun onDiaryItemVisibleStateTransitionCompleted() {
        updateToIdleState()
    }
    //endregion

    //region UI Event Handlers - Results
    /**
     * 日記読み込み確認ダイアログから結果を受け取った時に呼び出される事を想定。
     * 結果に応じて日記の読み込み、または天気情報の取得処理を開始する。
     * @param result ダイアログからの結果
     */
    internal fun onDiaryLoadDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryLoadDialogPositiveResult(pendingDiaryLoadParameters)
            }
            is DialogResult.Negative,
            is DialogResult.Cancel -> {
                handleDiaryLoadDialogNegativeResult(pendingDiaryLoadParameters)
            }
        }
        clearPendingDiaryLoadParameters()
    }

    /**
     * 日記読み込み確認ダイアログからのPositive結果を処理し、日記データを読み込む。
     * @param parameters 読み込みに必要なパラメータ
     */
    private fun handleDiaryLoadDialogPositiveResult(parameters: DiaryLoadParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                loadDiaryByDate(it.date)
            } ?: throw IllegalStateException()
        }
    }

    /**
     * 日記読み込み確認ダイアログからのNegative結果を処理し、天気情報を取得する。
     * @param parameters 天気情報取得に必要なパラメータ
     */
    private fun handleDiaryLoadDialogNegativeResult(parameters: DiaryLoadParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                fetchWeatherInfo(it.date, it.previousDate)
            } ?: throw IllegalStateException()
        }
    }

    /**
     * 日記上書き確認ダイアログから結果を受け取った時に呼び出される事を想定。
     * Positiveの場合のみ、日記の保存処理を開始する。
     * @param result ダイアログからの結果
     */
    internal fun onDiaryUpdateDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryUpdateDialogPositiveResult(pendingDiaryUpdateParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryUpdateParameters()
    }

    /**
     * 日記上書き確認ダイアログからのPositive結果を処理し、日記を保存する。
     * @param parameters 保存に必要なパラメータ
     */
    private fun handleDiaryUpdateDialogPositiveResult(parameters: DiaryUpdateParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                saveDiary(
                    it.diary,
                    it.diaryItemTitleSelectionHistoryList,
                    it.originalDiary,
                    it.isNewDiary
                )
            } ?: throw IllegalStateException()
        }
    }

    /**
     * 日記削除確認ダイアログから結果を受け取った時に呼び出される事を想定。
     * Positiveの場合のみ、日記の削除処理を開始する。
     * @param result ダイアログからの結果
     */
    internal fun onDiaryDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryDeleteDialogPositiveResult(pendingDiaryDeleteParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryDeleteParameters()
    }

    /**
     * 日記削除確認ダイアログからのPositive結果を処理し、日記を削除する。
     * @param parameters 削除に必要なパラメータ
     */
    private fun handleDiaryDeleteDialogPositiveResult(parameters: DiaryDeleteParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                deleteDiary(it.id, it.date)
            } ?: throw IllegalStateException()
        }
    }

    /**
     * 日付選択ダイアログから結果を受け取った時に呼び出される事を想定。
     * Positiveの場合のみ、日記の日付変更処理を開始する。
     * @param result ダイアログからの結果
     */
    internal fun onDatePickerDialogResultReceived(result: DialogResult<LocalDate>) {
        when (result) {
            is DialogResult.Positive<LocalDate> -> {
                handleDatePickerDialogPositiveResult(
                    result.data,
                    pendingDiaryDateUpdateParameters
                )
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryDateUpdateParameters()
    }

    /**
     * 日付選択ダイアログからのPositive結果を処理し、日記の日付を変更する。
     * @param date 選択された新しい日付
     * @param parameters 日付変更処理に必要なパラメータ
     */
    private fun handleDatePickerDialogPositiveResult(
        date: LocalDate,
        parameters: DiaryDateUpdateParameters?
    ) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                processChangedDiaryDate(date, it.originalDate, it.isNewDiary)
            } ?: throw IllegalStateException()
        }
    }

    /**
     * 日記読み込み失敗ダイアログから結果を受け取った時に呼び出される事を想定。
     * 前の画面へ戻るイベントを発行する。
     * @param result ダイアログからの結果
     */
    internal fun onDiaryLoadFailureDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit>,
            DialogResult.Negative,
            DialogResult.Cancel -> {
                launchWithUnexpectedErrorHandler {
                    emitUiEvent(
                        DiaryEditUiEvent.NavigatePreviousFragmentOnInitialDiaryLoadFailed()
                    )
                }
            }
        }
    }

    /**
     * 天気情報取得確認ダイアログから結果を受け取った時に呼び出される事を想定。
     * Positiveの場合のみ、権限確認処理を開始する。
     * @param result ダイアログからの結果
     */
    internal fun onWeatherInfoFetchDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleWeatherInfoFetchDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                clearPendingWeatherInfoFetchParameters()
            }
        }
    }

    /** 天気情報取得確認ダイアログからのPositive結果を処理し、権限を確認する。 */
    private fun handleWeatherInfoFetchDialogPositiveResult() {
        launchWithUnexpectedErrorHandler {
            checkPermissionBeforeWeatherInfoFetch()
        }
    }

    /**
     * 日記項目削除確認ダイアログから結果を受け取った時に呼び出される事を想定。
     * Positiveの場合のみ、日記項目の削除アニメーションを開始する。
     * @param result ダイアログからの結果
     */
    internal fun onDiaryItemDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleDiaryItemDeleteDialogPositiveResult(pendingDiaryItemDeleteParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingDiaryItemDeleteParameters()
    }

    /**
     * 日記項目削除確認ダイアログからのPositive結果を処理し、日記項目削除アニメーションを実行する。
     * @param parameters 項目削除に必要なパラメータ
     */
    private fun handleDiaryItemDeleteDialogPositiveResult(parameters: DiaryItemDeleteParameters?) {
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                requestDiaryItemDeleteTransition(it.itemNumber)
            } ?: throw IllegalStateException()
        }
    }

    /**
     * 添付画像削除確認ダイアログから結果を受け取った時に呼び出される事を想定。
     * Positiveの場合のみ、画像の削除処理を実行する。
     * @param result ダイアログからの結果
     */
    internal fun onDiaryImageDeleteDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive<Unit> -> {
                handleDiaryImageDeleteDialogPositiveResult()
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
    }

    /** 添付画像削除確認ダイアログからのPositive結果を処理し、 添付画像を削除する。*/
    private fun handleDiaryImageDeleteDialogPositiveResult() {
        launchWithUnexpectedErrorHandler {
            deleteImage()
        }
    }

    /**
     * 未保存終了確認ダイアログから結果を受け取った時に呼び出される事を想定。
     * Positiveの場合のみ、前の画面へ戻る。
     * @param result ダイアログからの結果
     */
    internal fun onExitWithoutDiarySaveDialogResultReceived(result: DialogResult<Unit>) {
        when (result) {
            is DialogResult.Positive -> {
                handleExitWithoutDiarySaveDialogPositiveResult(pendingPreviousNavigationParameters)
            }
            DialogResult.Negative,
            DialogResult.Cancel -> {
                // 処理なし
            }
        }
        clearPendingPreviousNavigationParameters()
    }

    /**
     * 未保存終了確認ダイアログからのPositive結果を処理し、前の画面へ遷移する。
     * @param parameters 前の画面へ戻るために必要なパラメータ
     */
    private fun handleExitWithoutDiarySaveDialogPositiveResult(
        parameters: PreviousNavigationParameters?
    ) {
        launchWithUnexpectedErrorHandler {
            clearDiaryImageCacheFile()
            parameters?.let {
                navigatePreviousFragment(it.originalDiaryDate)
            } ?: throw IllegalStateException()
        }
    }

    /**
     * 日記項目タイトル編集ダイアログから結果を受け取った時に呼び出される事を想定。
     * 項目タイトルを更新する。
     * @param result ダイアログからの結果
     */
    internal fun onItemTitleEditFragmentResultReceived(result: FragmentResult<DiaryItemTitleSelectionUi>) {
        when (result) {
            is FragmentResult.Some -> {
                updateItemTitle(result.data)
            }
            FragmentResult.None -> {
                // 処理なし
            }
        }
    }

    /**
     * ギャラリーから画像を選択した結果を受け取った時に呼び出される事を想定。
     * 選択された画像のキャッシュ処理を開始する。
     * @param uri 選択された画像のURI（未選択の場合はnull）
     */
    // MEMO:未選択時null
    internal fun onOpenDocumentResultImageUriReceived(uri: Uri?) {
        val parameters = pendingDiaryImageUpdateParameters
        clearPendingDiaryImageUpdateParameters()
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                cacheDiaryImage(uri, parameters.id)
            } ?: throw IllegalStateException()
        }
    }
    //endregion

    //region UI Event Handlers - Permissions
    /**
     * 位置情報権限の確認結果を受け取った時に呼び出される事を想定。
     * 権限が許可されている場合のみ、天気情報の取得処理を実行する。
     * @param isGranted 権限が許可されている場合はtrue
     */
    internal fun onAccessLocationPermissionChecked(
        isGranted: Boolean
    ) {
        val parameters = pendingWeatherInfoFetchParameters
        clearPendingWeatherInfoFetchParameters()
        launchWithUnexpectedErrorHandler {
            parameters?.let {
                executeFetchWeatherInfo(isGranted, it.date)
            } ?: throw IllegalStateException()
        }
    }
    //endregion

    //region Business Logic
    /**
     * 日記エントリの準備を行う。IDの有無で新規作成か既存の読み込みかを判断する。
     * @param id 既存の日記ID（新規の場合はnull）
     * @param date 対象の日付
     */
    private suspend fun prepareDiaryEntry(
        id: DiaryId?,
        date: LocalDate
    ) {
        if (id == null) {
            prepareNewDiaryEntry(date)
        } else {
            loadDiaryById(id, date)
        }
    }

    /**
     * 新規日記エントリの準備を行う。
     * @param date 作成する日記の日付
     */
    private suspend fun prepareNewDiaryEntry(date: LocalDate) {
        updateToNewDiaryState(date)
        val previousDate = currentUiState.previousSelectedDate
        val originalDate = originalDiary.date
        val isNewDiary = currentUiState.isNewDiary
        requestDiaryLoadConfirmation(
            date,
            previousDate,
            originalDate,
            isNewDiary
        )
    }

    /**
     * IDを指定して日記を読み込む。
     * @param id 読み込む日記のID
     * @param date 読み込む日記の日付
     */
    private suspend fun loadDiaryById(id: DiaryId, date: LocalDate) {
        executeDiaryLoad(
            id,
            date,
            { id, _ ->
                id ?: throw IllegalArgumentException()
                loadDiaryByIdUseCase(id)
            },
            { exception ->
                when (exception) {
                    is DiaryLoadByIdException.LoadFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryLoadFailure)
                    }
                    is DiaryLoadByIdException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    /**
     * 日付を指定して日記を読み込む。
     * @param date 読み込む日記の日付
     */
    private suspend fun loadDiaryByDate(date: LocalDate) {
        executeDiaryLoad(
            date = date,
            executeLoadDiary = { _, date ->
                loadDiaryByDateUseCase(date)
            },
            emitAppMessageOnFailure = { exception ->
                when (exception) {
                    is DiaryLoadByDateException.LoadFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryLoadFailure)
                    }
                    is DiaryLoadByDateException.Unknown -> {
                        emitUnexpectedAppMessage(exception)
                    }
                }
            }
        )
    }

    /**
     * 日記の読み込み処理を共通のロジックで実行する。
     * @param id 日記ID（既存の場合）
     * @param date 対象の日付
     * @param executeLoadDiary 実際の読み込みを行うUseCaseの実行処理
     * @param emitAppMessageOnFailure 読み込み失敗時のメッセージ発行処理
     */
    private suspend fun <E : UseCaseException> executeDiaryLoad(
        id: DiaryId? = null,
        date: LocalDate,
        executeLoadDiary: suspend (DiaryId?, LocalDate) -> UseCaseResult<Diary, E>,
        emitAppMessageOnFailure: suspend (E) -> Unit
    ) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        val previousState = currentUiState
        Log.i(logTag, "${logMsg}_previousState: $previousState")
        updateToDiaryLoadingState()
        when (val result = executeLoadDiary(id, date)) {
            is UseCaseResult.Success -> {
                updateToDiaryLoadSuccessState(result.value)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                if (previousState.originalDiaryLoadState == LoadState.Idle) {
                    updateToDiaryLoadErrorState()

                    // MEMO:連続するUIイベント（エラー表示と画面遷移）は、監視開始前に発行されると
                    //      取りこぼされる可能性がある。これを防ぐため、間に確認ダイアログを挟み、
                    //      ユーザーの応答を待ってから画面遷移を実行する。
                    emitUiEvent(
                        DiaryEditUiEvent.NavigateDiaryLoadFailureDialog(date)
                    )
                } else {
                    updateUiState { previousState }
                    emitAppMessageOnFailure(result.exception)
                }
            }
        }
    }

    /**
     * 日記データを保存する。
     * @param diary 保存する日記データ
     * @param diaryItemTitleSelectionHistoryList 項目タイトル選択履歴のリスト
     * @param originalDiary 保存前の元の日記データ
     * @param isNewDiary 新規日記の場合はtrue
     */
    private suspend fun saveDiary(
        diary: Diary,
        diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ) {
        val logMsg = "日記保存_"
        Log.i(logTag, "${logMsg}開始")

        updateToProcessingState()
        val result =
            saveDiaryUseCase(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            )
        when (result) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                updateToIdleState()
                clearDiaryImageCacheFile()
                emitUiEvent(
                    DiaryEditUiEvent
                        .NavigateDiaryShowFragment(diary.id.value, diary.date)
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateToIdleState()
                when (result.exception) {
                    is DiarySaveException.SaveFailure -> {
                        emitAppMessageEvent(
                            DiaryEditAppMessage.DiarySaveFailure
                        )
                    }
                    is DiarySaveException.InsufficientStorage -> {
                        emitAppMessageEvent(
                            DiaryEditAppMessage.DiarySaveInsufficientStorageFailure
                        )
                    }
                    is DiarySaveException.Unknown -> emitUnexpectedAppMessage(result.exception)
                }
            }
        }
    }

    /**
     * 指定されたIDの日記を削除する。
     * @param id 削除対象の日記ID
     * @param date 削除対象の日記の日付
     */
    private suspend fun deleteDiary(
        id: DiaryId,
        date: LocalDate
    ) {
        val logMsg = "日記削除_"
        Log.i(logTag, "${logMsg}開始")

        updateToProcessingState()
        when (val result = deleteDiaryUseCase(id)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                updateToIdleState()
                clearDiaryImageCacheFile()
                emitUiEvent(
                    DiaryEditUiEvent
                        .NavigatePreviousFragmentOnDiaryDelete(
                            FragmentResult.Some(date)
                        )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateToIdleState()
                when (result.exception) {
                    is DiaryDeleteException.DiaryDataDeleteFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryDeleteFailure)
                    }
                    is DiaryDeleteException.ImageFileDeleteFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryImageDeleteFailure)
                    }
                    is DiaryDeleteException.Unknown -> emitUnexpectedAppMessage(result.exception)
                }
            }
        }
    }

    /**
     * 既存日記の読み込みを確認するダイアログを表示するかどうかを判断し、必要に応じてダイアログを表示するイベントを発行する。
     * 表示不要の場合は天気情報の取得処理を開始する。
     * @param date 対象の日付
     * @param previousDate 以前選択されていた日付
     * @param originalDate 元の日記の日付
     * @param isNewDiary 新規日記の場合はtrue
     */
    private suspend fun requestDiaryLoadConfirmation(
        date: LocalDate,
        previousDate: LocalDate?,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        updateToProcessingState()
        val result =
            shouldRequestDiaryLoadConfirmationUseCase(date, previousDate, originalDate, isNewDiary)
        when (result) {
            is UseCaseResult.Success -> {
                updateToIdleState()
                if (result.value) {
                    updatePendingDiaryLoadParameters(date, previousDate)
                    emitUiEvent(
                        DiaryEditUiEvent.NavigateDiaryLoadDialog(date)
                    )
                } else {
                    fetchWeatherInfo(date, previousDate)
                }
            }
            is UseCaseResult.Failure -> {
                updateToIdleState()
                when (result.exception) {
                    is DiaryLoadConfirmationCheckException.CheckFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryLoadConfirmationCheckException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    /**
     * 日記の上書き保存を確認するダイアログを表示するかどうかを判断し、必要に応じてダイアログを表示するイベントを発行する。
     * 表示不要な場合は保存処理を実行する。
     * @param diary 保存する日記データ
     * @param diaryItemTitleSelectionHistoryList 項目タイトル選択履歴のリスト
     * @param originalDiary 保存前の元の日記データ
     * @param isNewDiary 新規日記の場合はtrue
     */
    private suspend fun requestDiaryUpdateConfirmation(
        diary: Diary,
        diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ) {
        updateToProcessingState()
        val date = diary.date
        val originalDate = originalDiary.date
        when (val result = shouldRequestDiaryUpdateConfirmationUseCase(date, originalDate, isNewDiary)) {
            is UseCaseResult.Success -> {
                updateToIdleState()
                if (result.value) {
                    updatePendingDiaryUpdateParameters(
                        diary,
                        diaryItemTitleSelectionHistoryList,
                        originalDiary,
                        isNewDiary
                    )
                    emitUiEvent(
                        DiaryEditUiEvent.NavigateDiaryUpdateDialog(diary.date)
                    )
                } else {
                    saveDiary(
                        diary,
                        diaryItemTitleSelectionHistoryList,
                        originalDiary,
                        isNewDiary
                    )
                }
            }
            is UseCaseResult.Failure -> {
                updateToIdleState()
                when (result.exception) {
                    is DiaryUpdateConfirmationCheckException.CheckFailure -> {
                        emitAppMessageEvent(
                            DiaryEditAppMessage.DiarySaveFailure
                        )
                    }
                    is DiaryUpdateConfirmationCheckException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    /**
     * 天気情報の取得を開始する。天気情取得設定が無効の場合は何もしない。
     * @param date 天気情報を取得する日付
     * @param previousDate 以前選択されていた日付
     */
    // 天気情報取得関係
    private suspend fun fetchWeatherInfo(date: LocalDate, previousDate: LocalDate?) {
        val isEnabled = checkWeatherInfoFetchEnabledUseCase().value
        if (!isEnabled) return

        requestWeatherInfoConfirmation(date, previousDate)
    }

    /**
     * 天気情報取得の確認ダイアログを表示するか判断し、必要に応じてダイアログ表示イベントを発行する。
     * 表示が不要な場合は、権限確認を要求するイベントを発行する。
     * @param date 天気情報を取得する日付
     * @param previousDate 以前選択されていた日付
     */
    private suspend fun requestWeatherInfoConfirmation(
        date: LocalDate,
        previousDate: LocalDate?
    ) {
        val shouldRequest =
            shouldRequestWeatherInfoConfirmationUseCase(date, previousDate).value
        if (shouldRequest) {
            updatePendingWeatherInfoFetchParameters(date)
            emitUiEvent(
                DiaryEditUiEvent.NavigateWeatherInfoFetchDialog(date)
            )
        } else {
            val shouldLoad = shouldFetchWeatherInfoUseCase(date, previousDate).value
            if (!shouldLoad) return

            updatePendingWeatherInfoFetchParameters(date)
            checkPermissionBeforeWeatherInfoFetch()
        }
    }

    // TODO過剰プライベートメソッド？
    /** 位置情報権限の確認を要求するUIイベントを発行する。 */
    private suspend fun checkPermissionBeforeWeatherInfoFetch() {
        emitUiEvent(
            DiaryEditUiEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch
        )
    }

    /**
     * 天気情報の取得を実行する。
     * @param isGranted 位置情報権限が許可されているか
     * @param date 天気情報を取得する日付
     */
    private suspend fun executeFetchWeatherInfo(
        isGranted: Boolean,
        date: LocalDate
    ) {
        if (!isGranted) {
            updateToIdleState()
            emitAppMessageEvent(DiaryEditAppMessage.AccessLocationPermissionRequest)
        }

        updateToProcessingState()
        when (val result = fetchWeatherInfoUseCase(date)) {
            is UseCaseResult.Success -> {
                updateToIdleState()
                updateWeather1(result.value.toUiModel())
                updateWeather2(Weather.UNKNOWN.toUiModel())
            }
            is UseCaseResult.Failure -> {
                updateToIdleState()
                when (result.exception) {
                    is WeatherInfoFetchException.LocationPermissionNotGranted -> {
                        emitAppMessageEvent(
                            DiaryEditAppMessage.AccessLocationPermissionRequest
                        )
                    }
                    is WeatherInfoFetchException.DateOutOfRange -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoDateOutOfRange)
                    }
                    is WeatherInfoFetchException.LocationAccessFailure,
                    is WeatherInfoFetchException.FetchFailure -> {
                        emitAppMessageEvent(DiaryEditAppMessage.WeatherInfoFetchFailure)
                    }
                    is WeatherInfoFetchException.Unknown -> emitUnexpectedAppMessage(result.exception)
                }
            }
        }
    }

    /**
     * 変更された日記の日付を更新し、必要に応じて既存の日記の読み込み確認を行う。
     * @param date 新しい日付
     * @param originalDate 元の日付
     * @param isNewDiary 新規日記の場合はtrue
     */
    // 日付関係
    private suspend fun processChangedDiaryDate(
        date: LocalDate,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        updateDate(date)
        val previousDate = currentUiState.previousSelectedDate
        // MEMO:下記処理をdate(StateFlow)変数のCollectorから呼び出すと、
        //      画面回転時にも不要に呼び出してしまう為、下記にて処理。
        requestDiaryLoadConfirmation(
            date,
            previousDate,
            originalDate,
            isNewDiary
        )
    }

    /** 日記に新しい項目を追加する。 */
    // 項目関係
    // MEMO:日記項目追加処理完了時のUi更新(編集中)は日記項目追加完了イベントメソッドにて処理
    private suspend fun addDiaryItem() {
        updateToInputDisabledState()
        emitUiEvent(DiaryEditUiEvent.ItemAddition)
        val numVisibleItems = currentUiState.numVisibleDiaryItems
        val additionItemNumber = numVisibleItems + 1
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(
                    itemTitles = it.editingDiary.itemTitles + (additionItemNumber to ""),
                    itemComments = it.editingDiary.itemComments + (additionItemNumber to "")
                )
            )
        }
    }

    /**
     * 日記項目の削除アニメーションのイベントを発行する。
     * 削除対象が項目1で、現在表示されている項目が項目1のみの場合、アニメーションイベントを発行せずにデータのみを削除する。
     * @param itemNumber 削除する項目の番号
     */
    // MEMO:日記項目削除処理完了時のUi更新(編集中)は日記項目削除メソッドにて処理
    private suspend fun requestDiaryItemDeleteTransition(itemNumber: DiaryItemNumber) {
        val numVisibleItems = currentUiState.numVisibleDiaryItems

        updateToInputDisabledState()
        if (itemNumber.isMinNumber && itemNumber.value == numVisibleItems) {
            deleteItem(itemNumber)
        } else {
            emitUiEvent(
                DiaryEditUiEvent.TransitionDiaryItemToInvisibleState(itemNumber.value)
            )
        }
    }

    /**
     * 日記項目データを削除し、後続の項目データを繰り上げる。
     * （例：項目2を削除した場合、項目3～5のデータを項目2～4へ繰り上げる）
     * @param itemNumber 削除する項目の番号
     */
    // MEMO:日記項目削除処理開始時のUi更新(項目削除中)は日記項目削除トランジション要求メソッドにて処理
    private fun deleteItem(itemNumber: DiaryItemNumber) {
        val currentEditingDiary = currentUiState.editingDiary
        val updateItemTitles = currentEditingDiary.itemTitles.toMutableMap()
        val updateItemComments = currentEditingDiary.itemComments.toMutableMap()
        val updateHistories = currentUiState.diaryItemTitleSelectionHistories.toMutableMap()

        if (itemNumber.isMinNumber) {
            updateItemTitles[itemNumber.value] = ""
            updateItemComments[itemNumber.value] = ""
        } else {
            updateItemTitles[itemNumber.value] = null
            updateItemComments[itemNumber.value] = null
        }
        updateHistories[itemNumber.value] = null

        val currentNumVisibleItems = currentUiState.numVisibleDiaryItems
        if (itemNumber.value < currentNumVisibleItems) {
            for (i in itemNumber.value until currentNumVisibleItems) {
                val targetItemNumber = i
                val nextItemNumber = targetItemNumber.inc()

                updateItemTitles[targetItemNumber] = updateItemTitles[nextItemNumber]
                updateItemTitles[nextItemNumber] = null
                updateItemComments[targetItemNumber] = updateItemComments[nextItemNumber]
                updateItemComments[nextItemNumber] = null
                updateHistories[targetItemNumber] = updateHistories[nextItemNumber]
                updateHistories[nextItemNumber] = null
            }
        }

        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(
                    itemTitles = updateItemTitles,
                    itemComments = updateItemComments
                ),
                diaryItemTitleSelectionHistories = updateHistories
            )
        }
        updateToIdleState()
    }

    /** 画像を選択させるギャラリーを開くイベントを発行する。 */
    // 添付画像関係
    // MEMO:画像選択完了時のUi更新(編集中)は画像選択完了イベントメソッドにて処理
    private suspend fun selectImage(diaryId: DiaryId) {
        updateToProcessingState()
        updatePendingDiaryImageUpdateParameters(diaryId)
        emitUiEvent(DiaryEditUiEvent.SelectImage)
    }

    /** 添付画像を削除（UI状態の更新とキャッシュファイルの削除）する。 */
    private suspend fun deleteImage() {
        updateImageFileName(null)
        clearDiaryImageCacheFile()
    }

    /**
     * 選択された画像をキャッシュし、ファイル名をUI状態に保存する。
     * @param uri 選択された画像のURI
     * @param diaryId 対象の日記ID
     */
    private suspend fun cacheDiaryImage(uri: Uri?, diaryId: DiaryId) {
        if (uri != null) {
            val result =
                cacheDiaryImageUseCase(uri.toString(), diaryId)
            when (result) {
                is UseCaseResult.Success -> {
                    updateImageFileName(result.value.fullName)
                }
                is UseCaseResult.Failure -> {
                    when (result.exception) {
                        is DiaryImageCacheException.CacheFailure -> {
                            emitAppMessageEvent(
                                DiaryEditAppMessage.ImageLoadFailure
                            )
                        }
                        is DiaryImageCacheException.InsufficientStorage -> {
                            emitAppMessageEvent(
                                DiaryEditAppMessage.ImageLoadInsufficientStorageFailure
                            )
                        }
                        is DiaryImageCacheException.Unknown -> {
                            emitUnexpectedAppMessage(result.exception)
                        }
                    }
                }
            }
        }
        updateToIdleState()
    }

    /** 添付されてた画像キャッシュファイルを削除する。 */
    private suspend fun clearDiaryImageCacheFile() {
        updateToProcessingState()
        when (val result = clearDiaryImageCacheFileUseCase()) {
            is UseCaseResult.Success -> {
                updateToIdleState()
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "画像キャッシュファイルクリア失敗", result.exception)
                updateToIdleState()
                when (result.exception) {
                    is DiaryImageCacheFileClearException.ClearFailure -> {
                        // ユーザーには直接関わらない処理の為、通知不要
                    }
                    is DiaryImageCacheFileClearException.Unknown -> {
                        emitUnexpectedAppMessage(result.exception)
                    }
                }
            }
        }
    }

    /**
     * 戻るナビゲーションの処理を行う。変更がある場合は確認ダイアログを表示する。
     * @param diary 現在の編集中の日記データ
     * @param originalDiary 元の日記データ
     */
    private suspend fun handleBackNavigation(
        diary: Diary,
        originalDiary: Diary
    ) {
        val shouldRequest = shouldRequestExitWithoutDiarySaveConfirmationUseCase(
            diary,
            originalDiary
        ).value
        if (shouldRequest) {
            updatePendingPreviousNavigationParameter(originalDiary.date)
            emitUiEvent(
                DiaryEditUiEvent.NavigateExitWithoutDiarySaveConfirmationDialog
            )
        } else {
            clearDiaryImageCacheFile()
            navigatePreviousFragment(originalDiary.date)
        }
    }

    /**
     * 前の画面へ遷移するイベントを発行する。
     * @param originalDiaryDate 遷移元に返す日付
     */
    private suspend fun navigatePreviousFragment(originalDiaryDate: LocalDate) {
        emitNavigatePreviousFragmentEvent(
            FragmentResult.Some(originalDiaryDate)
        )
    }
    //endregion

    //region UI State Update - Property
    /**
     * 日記の日付を更新する。
     * @param date 新しい日付
     */
    private fun updateDate(date: LocalDate) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(date = date),
                previousSelectedDate = it.editingDiary.date
            )
        }
    }

    /**
     * 日記タイトルを更新する。
     * @param title 新しいタイトル
     */
    private fun updateTitle(title: String) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(title = title)
            )
        }
    }

    /**
     * 天気1を更新する。
     * @param weather 新しい天気
     */
    private fun updateWeather1(weather: WeatherUi) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(weather1 = weather)
            )
        }
    }

    /**
     * 天気2を更新する。
     * @param weather 新しい天気
     */
    private fun updateWeather2(weather: WeatherUi) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(weather2 = weather)
            )
        }
    }

    /**
     * 体調を更新する。
     * @param condition 新しい体調
     */
    private fun updateCondition(condition: ConditionUi) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(condition = condition)
            )
        }
    }

    /**
     * 表示されている日記項目数を更新する。
     * @param num 新しい項目数
     */
    private fun updateNumVisibleItems(num: Int) {
        updateUiState { it.copy(numVisibleDiaryItems = num) }
    }

    /**
     * 日記項目のタイトルを更新する。
     * @param itemNumberInt 対象の項目番号
     * @param title 新しいタイトル
     */
    private fun updateItemTitle(itemNumberInt: Int, title: String) {
        updateUiState {
            it.copy(
                editingDiary =
                    it.editingDiary.copy(
                        itemTitles = it.editingDiary.itemTitles + (itemNumberInt to title)
                    )
            )
        }
    }

    /**
     * 日記項目タイトル選択履歴（日記項目タイトル編集画面より）から選択された日記項目のタイトルを更新する。
     * @param selection 選択されたタイトル情報
     */
    private fun updateItemTitle(selection: DiaryItemTitleSelectionUi) {
        val itemNumberInt = selection.itemNumber
        val title = selection.title
        val updateHistory = selection.let {
            DiaryItemTitleSelectionHistoryUi(
                it.id ?: throw IllegalStateException(),
                it.title,
                LocalDateTime.now()
            )
        }
        updateUiState {
            it.copy(
                editingDiary =
                    it.editingDiary.copy(
                        itemTitles = it.editingDiary.itemTitles + (itemNumberInt to title)
                    ),
                diaryItemTitleSelectionHistories =
                    it.diaryItemTitleSelectionHistories + (itemNumberInt to updateHistory)
            )
        }
    }

    /**
     * 日記項目のコメントを更新する。
     * @param itemNumberInt 対象の項目番号
     * @param comment 新しいコメント
     */
    private fun updateItemComment(
        itemNumberInt: Int,
        comment: String
    ) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(
                    itemComments = it.editingDiary.itemComments + (itemNumberInt to comment)
                )
            )
        }
    }

    /**
     * 添付画像のファイル名を更新する。
     * @param imageFileName 新しい画像ファイル名
     */
    private fun updateImageFileName(imageFileName: String?) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(imageFileName = imageFileName)
            )
        }
    }

    /**
     * 最終更新日時を更新する。
     * @param log 新しい最終更新日時
     */
    private fun updateLog(log: LocalDateTime) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(log = log)
            )
        }
    }

    /**
     * 天気2の選択肢を更新する。
     * @param options 新しい選択肢リスト
     */
    private fun updateWeather2Options(options: List<WeatherUi>) {
        updateUiState { it.copy(weather2Options = options) }
    }

    /**
     * 日記項目追加ボタンの有効/無効状態を更新する。
     * @param isEnabled 有効にする場合はtrue
     */
    private fun updateIsDiaryItemAdditionEnabled(isEnabled: Boolean) {
        updateUiState { it.copy(isDiaryItemAdditionEnabled = isEnabled) }
    }

    /**
     * 添付画像のファイルパスを更新する。
     * @param path 新しいファイルパス
     */
    private fun updateDiaryImageFilePath(path: FilePathUi?) {
        updateUiState { it.copy(diaryImageFilePath = path) }
    }
    //endregion

    //region UI State Update - State
    /** UIをアイドル状態（操作可能）に更新する。 */
    private fun updateToIdleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
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

    /** UIを入力無効の状態（プログレスインディケータ非表示）に更新する。 */
    private fun updateToInputDisabledState() {
        updateUiState {
            it.copy(
                isInputDisabled = true
            )
        }
    }

    /**
     * UIを新規日記作成の状態に更新する。
     * @param date 新規作成する日記の日付
     */
    private fun updateToNewDiaryState(date: LocalDate) {
        val newDiary = Diary.generate().toUiModel().copy(date = date)
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Success(newDiary),
                editingDiary = newDiary,
                isNewDiary = true
            )
        }
    }private fun updateToDiaryLoadingState() {
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Loading,
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    /**
     * UIを日記読み込み成功の状態に更新する。
     * @param diary 読み込んだ日記データ
     */
    private fun updateToDiaryLoadSuccessState(diary: Diary) {
        val diaryUi = diary.toUiModel()
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Success(diaryUi),
                editingDiary = diaryUi,
                isNewDiary = false,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    /** UIを日記読み込み失敗の状態に更新する。 */
    private fun updateToDiaryLoadErrorState() {
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Error,
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }

    /** 天気2のUIを有効な状態に更新する。 */
    private fun updateToWeather2EnabledState() {
        updateUiState { it.copy(isWeather2Enabled = true) }
    }

    /** UIを天気2のUIを無効な状態に更新する。 */
    private fun updateToWeather2DisabledState() {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(weather2 = WeatherUi.UNKNOWN),
                isWeather2Enabled = false
            )
        }
    }
    //endregion

    //region Pending Diary Load Parameters
    /**
     * 保留中の日記読み込みパラメータを更新する。
     * @param date 読み込み対象の日付
     * @param previousDate 以前選択されていた日付
     */
    private fun updatePendingDiaryLoadParameters(date: LocalDate, previousDate: LocalDate?) {
        pendingDiaryLoadParameters = DiaryLoadParameters(date, previousDate)
    }

    /** 保留中の日記読み込みパラメータをクリアする。 */
    private fun clearPendingDiaryLoadParameters() {
        pendingDiaryLoadParameters = null
    }

    /**
     * 日記読み込み処理に必要なパラメータを保持するデータクラス。
     * @property date 読み込み対象の日付
     * @property previousDate 以前選択されていた日付
     */
    private data class DiaryLoadParameters(
        val date: LocalDate,
        val previousDate: LocalDate?
    )
    //endregion

    //region Pending Diary Update Parameters
    /**
     * 保留中の日記更新パラメータを更新する。
     * @param diary 保存する日記データ
     * @param diaryItemTitleSelectionHistoryList 項目タイトル選択履歴のリスト
     * @param originalDiary 保存前の元の日記データ
     * @param isNewDiary 新規日記の場合はtrue
     */
    private fun updatePendingDiaryUpdateParameters(
        diary: Diary,
        diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        originalDiary: Diary,
        isNewDiary: Boolean
    ) {
        pendingDiaryUpdateParameters =
            DiaryUpdateParameters(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            )
    }

    /** 保留中の日記更新パラメータをクリアする。 */
    private fun clearPendingDiaryUpdateParameters() {
        pendingDiaryUpdateParameters = null
    }

    /**
     * 日記更新処理に必要なパラメータを保持するデータクラス。
     * @property diary 保存する日記データ
     * @property diaryItemTitleSelectionHistoryList 項目タイトル選択履歴のリスト
     * @property originalDiary 保存前の元の日記データ
     * @property isNewDiary 新規日記の場合はtrue
     */
    private data class DiaryUpdateParameters(
        val diary: Diary,
        val diaryItemTitleSelectionHistoryList: List<DiaryItemTitleSelectionHistory>,
        val originalDiary: Diary,
        val isNewDiary: Boolean
    )
    //endregion

    //region Pending Diary Delete Parameters
    /**
     * 保留中の日記削除パラメータを更新する。
     * @param id 削除対象の日記ID
     * @param date 削除対象の日記の日付
     */
    private fun updatePendingDiaryDeleteParameters(id: DiaryId, date: LocalDate) {
        pendingDiaryDeleteParameters = DiaryDeleteParameters(id, date)
    }

    /** 保留中の日記削除パラメータをクリアする。 */
    private fun clearPendingDiaryDeleteParameters() {
        pendingDiaryDeleteParameters = null
    }

    /**
     * 日記削除処理に必要なパラメータを保持するデータクラス。
     * @property id 削除対象の日記ID
     * @property date 削除対象の日記の日付
     */
    private data class DiaryDeleteParameters(
        val id: DiaryId,
        val date: LocalDate
    )
    //endregion

    //region Pending Diary Date Update Parameters
    /**
     * 保留中の日記日付更新パラメータを更新する。
     * @param originalDate 元の日付
     * @param isNewDiary 新規日記の場合はtrue
     */
    private fun updatePendingDiaryDateUpdateParameters(
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        pendingDiaryDateUpdateParameters = DiaryDateUpdateParameters(originalDate, isNewDiary)
    }

    /** 保留中の日記日付更新パラメータをクリアする。 */
    private fun clearPendingDiaryDateUpdateParameters() {
        pendingDiaryDateUpdateParameters = null
    }

    /**
     * 日記日付更新処理に必要なパラメータを保持するデータクラス。
     * @property originalDate 元の日付
     * @property isNewDiary 新規日記の場合はtrue
     */
    private data class DiaryDateUpdateParameters(
        val originalDate: LocalDate,
        val isNewDiary: Boolean
    )
    //endregion

    //region Pending Previous Navigation Parameters
    /**
     * 保留中の前画面遷移パラメータを更新する。
     * @param originalDiaryDate 元の日記の日付
     */
    private fun updatePendingPreviousNavigationParameter(originalDiaryDate: LocalDate) {
        pendingPreviousNavigationParameters = PreviousNavigationParameters(originalDiaryDate)
    }

    /** 保留中の前画面遷移パラメータをクリアする。 */
    private fun clearPendingPreviousNavigationParameters() {
        pendingPreviousNavigationParameters = null
    }

    /**
     * 日記項目削除処理に必要なパラメータを保持するデータクラス。
     * @property itemNumber 削除する項目の番号
     */
    private data class DiaryItemDeleteParameters(
        val itemNumber: DiaryItemNumber
    )
    //endregion

    //region Pending Weather Info Fetch Parameters
    /**
     * 保留中の天気情報取得パラメータを更新する。
     * @param date 取得対象の日付
     */
    private fun updatePendingWeatherInfoFetchParameters(date: LocalDate) {
        pendingWeatherInfoFetchParameters = WeatherInfoFetchParameters(date)
    }

    /** 保留中の天気情報取得パラメータをクリアする。 */
    private fun clearPendingWeatherInfoFetchParameters() {
        pendingWeatherInfoFetchParameters = null
    }

    /**
     * 日記画像更新処理に必要なパラメータを保持するデータクラス。
     * @property id 対象の日記ID
     */
    private data class DiaryImageUpdateParameters(val id: DiaryId)
    //endregion

    //region Pending Diary Item Delete Parameters
    /**
     * 保留中の日記項目削除パラメータを更新する。
     * @param itemNumber 削除する項目の番号
     */
    private fun updatePendingDiaryItemDeleteParameters(itemNumber: DiaryItemNumber) {
        pendingDiaryItemDeleteParameters = DiaryItemDeleteParameters(itemNumber)
    }

    /** 保留中の日記項目削除パラメータをクリアする。 */
    private fun clearPendingDiaryItemDeleteParameters() {
        pendingDiaryItemDeleteParameters = null
    }

    /**
     * 天気情報取得処理に必要なパラメータを保持するデータクラス。
     * @property date 取得対象の日付
     */
    private data class WeatherInfoFetchParameters(
        val date: LocalDate
    )
    //endregion

    //region Pending Diary Image Update Parameters
    /**
     * 保留中の日記画像更新パラメータを更新する。
     * @param diaryId 対象の日記ID
     */
    private fun updatePendingDiaryImageUpdateParameters(diaryId: DiaryId) {
        pendingDiaryImageUpdateParameters = DiaryImageUpdateParameters(diaryId)
    }

    /** 保留中の日記画像更新パラメータをクリアする。 */
    private fun clearPendingDiaryImageUpdateParameters() {
        pendingDiaryImageUpdateParameters = null
    }

    /**
     * 前画面遷移処理に必要なパラメータを保持するデータクラス。
     * @property originalDiaryDate 元の日記の日付
     */
    private data class PreviousNavigationParameters(
        val originalDiaryDate: LocalDate
    )
    //endregion

    private companion object {
        /** ナビゲーションコンポーネントで受け渡される日記IDのキー。 */
        const val ARGUMENT_DIARY_ID_KEY = "diary_id"

        /** ナビゲーションコンポーネントで受け渡される日記日付のキー。 */
        const val ARGUMENT_DIARY_DATE_KEY = "diary_date"

        /** SavedStateHandleにUI状態を保存するためのキー。 */
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }

    //region For Test
    // TODO:テスト用の為、最終的に削除
    var isTesting = false

    fun test() {
        launchWithUnexpectedErrorHandler {
            isTesting = true
            val startDate = currentUiState.editingDiary.date
            for (i in 0 until 10) {
                val saveDate = startDate.minusDays(i.toLong())

                when (val result = doesDiaryExistUseCase(saveDate)) {
                    is UseCaseResult.Success -> {
                        if (result.value) continue
                    }
                    is UseCaseResult.Failure -> {
                        when (result.exception) {
                            is DiaryExistenceCheckException.CheckFailure -> {
                                emitAppMessageEvent(DiaryEditAppMessage.DiaryInfoLoadFailure)
                            }
                            is DiaryExistenceCheckException.Unknown -> {
                                emitUnexpectedAppMessage(result.exception)
                            }
                        }
                        isTesting = false
                        return@launchWithUnexpectedErrorHandler
                    }
                }
                updateUiState {
                    DiaryEditUiState(editingDiary = Diary.generate().toUiModel())
                }
                updateToNewDiaryState(saveDate)

                // ランダムな天気と体調を設定
                val weather1Options = uiState.value.weather1Options
                updateWeather1(weather1Options.random())
                val weather2Options = uiState.value.weather2Options
                updateWeather2(weather2Options.random())
                val conditionOptions = uiState.value.conditionOptions
                updateCondition(conditionOptions.random())

                // パターン化された日記データを取得
                val testData = getTestDiaryDataPattern(i)

                // 日記タイトルを設定
                updateTitle(testData.diaryTitle)

                // 日記項目数と内容を設定
                updateNumVisibleItems(testData.items.size)
                testData.items.forEachIndexed { index, item ->
                    val diaryItemNumber = index + 1
                    val selection = DiaryItemTitleSelectionUi(
                        diaryItemNumber,
                        DiaryItemTitleSelectionHistoryId.generate().value,
                        item.itemTitle
                    )
                    updateItemTitle(selection)
                    updateItemComment(diaryItemNumber, item.itemComment)
                }


                val diary = currentUiState.editingDiary.toDomainModel()
                val diaryItemTitleSelectionHistoryList =
                    currentUiState.diaryItemTitleSelectionHistories
                        .values.filterNotNull().map { it.toDomainModel() }
                val originalDiary = originalDiary.toDomainModel()
                val isNewDiary = currentUiState.isNewDiary

                val result =
                    saveDiaryUseCase(
                        diary,
                        diaryItemTitleSelectionHistoryList,
                        originalDiary,
                        isNewDiary
                    )
                when (result) {
                    is UseCaseResult.Success -> {
                        // 処理なし
                    }
                    is UseCaseResult.Failure -> {
                        isTesting = false
                        updateToIdleState()
                        when (result.exception) {
                            is DiarySaveException.SaveFailure -> {
                                emitAppMessageEvent(
                                    DiaryEditAppMessage.DiarySaveFailure
                                )
                            }
                            is DiarySaveException.InsufficientStorage -> {
                                emitAppMessageEvent(
                                    DiaryEditAppMessage.DiarySaveInsufficientStorageFailure
                                )
                            }
                            is DiarySaveException.Unknown -> emitUnexpectedAppMessage(result.exception)
                        }
                        return@launchWithUnexpectedErrorHandler
                    }
                }
            }
            clearDiaryImageCacheFile()
            navigatePreviousFragment(originalDiary.date)
            isTesting = false
        }
    }

    private fun generateRandomAlphanumericString(length: Int): String {
        require(length >= 0) { "Length must be non-negative" }

        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * テスト用の日記データ一式を保持するデータクラス。
     */
    data class TestDiaryData(
        val diaryTitle: String,
        val items: List<TestDiaryItem>
    )

    /**
     * テスト用の日記項目データ一式を保持するデータクラス。
     */
    data class TestDiaryItem(
        val itemTitle: String,
        val itemComment: String
    )

    /**
     * 関連性のある日記データ一式のセットを、指定されたパターン番号で取得する。
     * @param patternNumber 0から29の間のパターン番号。
     * @return 指定されたパターンの日記データセット。
     */
    private fun getTestDiaryDataPattern(patternNumber: Int): TestDiaryData {
        // 30パターンの日記データテンプレート
        val diaryTemplates = listOf(
            // パターン0: カフェ巡り (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("新しいカフェを発見", "駅裏の路地を入ったところに、雰囲気の良いカフェを見つけた。コーヒーが絶品。"),
                    TestDiaryItem("読書タイム", "買ったばかりの小説を読み進めた。物語の世界に没頭できた。"),
                    TestDiaryItem("今日のケーキ", "チーズケーキを注文。濃厚でクリーミー、最高の味だった。")
                )
                TestDiaryData("お気に入りのカフェ巡り", items)
            },
            // パターン1: 映画鑑賞 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("話題のSF大作", "映像美がとにかく凄かった。特に宇宙船のシーンは圧巻。"),
                    TestDiaryItem("ストーリーの感想", "少し難解な部分もあったけど、伏線が回収されていくのが見事。"),
                    TestDiaryItem("心に残ったセリフ", "「未来は決まっていない、君が作るんだ」という言葉が胸に響いた。"),
                    TestDiaryItem("映画館のポップコーン", "塩バター味のポップコーンが映画の最高のお供だった。")
                )
                TestDiaryData("映画館で過ごす休日", items)
            },
            // パターン2: 筋トレ (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("ベンチプレス", "自己ベストを更新！60kgを3回持ち上げられた。"),
                    TestDiaryItem("スクワット", "フォームを意識して丁寧に。下半身にしっかり効いた。"),
                    TestDiaryItem("プロテイン摂取", "トレーニング後のゴールデンタイムにプロテインを補給。チョコ味が美味しい。"),
                    TestDiaryItem("今日の体調", "少し疲労感はあるけど、達成感がすごい。筋肉痛が楽しみだ。"),
                    TestDiaryItem("ジムの混雑具合", "平日の昼間は空いていて、マシンが使い放題で快適だった。")
                )
                TestDiaryData("今日のジムトレーニング記録", items)
            },
            // パターン3: 料理 (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("パスタに挑戦", "レシピ動画を見ながらカルボナーラを作った。"),
                    TestDiaryItem("出来栄えは？", "卵が少し固まってしまったけど、味はなかなか。家族にも好評だった。")
                )
                TestDiaryData("手作りカルボナーラ", items)
            },
            // パターン4: 散歩 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("近所の公園", "天気が良かったので、近くの公園まで散歩。金木犀の香りがした。"),
                    TestDiaryItem("見かけた猫", "ベンチで日向ぼっこしている猫がいて癒された。"),
                    TestDiaryItem("今日の歩数", "アプリで見たら8000歩も歩いていた。良い運動になった。")
                )
                TestDiaryData("秋の散歩日和", items)
            },
            // パターン5: 新しい挑戦 (項目数: 1)
            {
                val items = listOf(
                    TestDiaryItem("プログラミング学習", "Kotlinの新しいライブラリを触ってみた。覚えることが多くて大変だが、面白い。")
                )
                TestDiaryData("新しいスキル習得への道", items)
            },
            // パターン6:読書 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("ミステリー小説", "東野圭吾の新作を一気読み。最後のどんでん返しに鳥肌が立った。"),
                    TestDiaryItem("心に残った一文", "「絶望の淵でこそ、人は最も強く輝く」という言葉が印象的だった。"),
                    TestDiaryItem("次の本", "次は自己啓発本を読んでみようか検討中。"),
                    TestDiaryItem("読書環境", "静かな部屋で、温かいコーヒーを飲みながら読むのが至福の時間。")
                )
                TestDiaryData("読書に没頭した一日", items)
            },
            // パターン7: 買い物 (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("秋服を探しに", "デパートをぶらぶら。チェック柄のシャツと新しいジャケットを購入。"),
                    TestDiaryItem("衝動買い", "買う予定はなかったけど、デザインが気に入ってスニーカーも買ってしまった。")
                )
                TestDiaryData("ショッピングで気分転換", items)
            },
            // パターン8: 勉強・学習 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("資格試験の勉強", "模擬試験を解いてみた。合格ラインにはまだ少し届かない。"),
                    TestDiaryItem("苦手分野", "計算問題で時間を使いすぎているのが課題。重点的に復習が必要だ。"),
                    TestDiaryItem("集中力", "2時間集中して勉強できた。ポモドーロテクニックが効果的かもしれない。")
                )
                TestDiaryData("資格取得に向けた勉強記録", items)
            },
            // パターン9: ガーデニング (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("ベランダ菜園", "育てているミニトマトが赤くなってきた。収穫が楽しみ。"),
                    TestDiaryItem("水やり", "朝晩の涼しい時間にたっぷり水をあげた。植物が元気だと嬉しい。"),
                    TestDiaryItem("新しい仲間", "ハーブの苗を新しく購入。バジルとローズマリーを植えた。"),
                    TestDiaryItem("虫対策", "アブラムシが少し付いていたので、専用のスプレーで駆除した。"),
                    TestDiaryItem("成長の記録", "毎日の変化を写真に撮って記録するのも楽しい。")
                )
                TestDiaryData("ベランダでのガーデニング日和", items)
            },
            // パターン10: 友人との時間 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("久しぶりの再会", "高校時代の友人とランチ。何年経っても変わらない関係が嬉しい。"),
                    TestDiaryItem("思い出話", "昔の笑える失敗談で盛り上がった。時間が経つのがあっという間。"),
                    TestDiaryItem("近況報告", "お互いの仕事やプライベートについて語り合った。良い刺激をもらえた。")
                )
                TestDiaryData("親友との楽しいひととき", items)
            },
            // パターン11: ペットとのふれあい (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("犬の散歩", "夕方に長めの散歩へ。他の犬と楽しそうに挨拶していた。"),
                    TestDiaryItem("新しいおもちゃ", "新しいボールを買ってあげたら、夢中になって遊んでいる。")
                )
                TestDiaryData("愛犬とののんびりした一日", items)
            },
            // パターン12: 健康・体調 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("少し風邪気味", "朝から少し喉が痛い。早めに薬を飲んで対策。"),
                    TestDiaryItem("食事", "消化に良いものを食べようと思い、夜はお粥にした。"),
                    TestDiaryItem("休息", "今日は無理せず、早めにベッドに入ってゆっくり休むことにした。"),
                    TestDiaryItem("ビタミン補給", "フルーツを多めに食べてビタミンを補給した。")
                )
                TestDiaryData("体調管理を意識した日", items)
            },
            // パターン13: 整理整頓 (項目数: 1)
            {
                val items = listOf(
                    TestDiaryItem("クローゼットの整理", "思い切って断捨離。もう着ない服を処分したら、かなりスッキリした。")
                )
                TestDiaryData("断捨離で心もスッキリ", items)
            },
            // パターン14: 美容・セルフケア (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("美容院へ", "髪を10cmほどカットして、カラーも秋色にチェンジ。気分が変わる。"),
                    TestDiaryItem("スキンケア", "いつもより時間をかけて丁寧にスキンケア。パックで肌がもちもちになった。"),
                    TestDiaryItem("半身浴", "好きな香りの入浴剤を入れて、ゆっくり半身浴。最高のリラックスタイム。"),
                    TestDiaryItem("ネイルケア", "爪やすりで形を整えて、新しいネイルカラーを塗った。"),
                    TestDiaryItem("マッサージ", "セルフマッサージで足のむくみを取った。体が軽くなった気がする。")
                )
                TestDiaryData("自分を労わるセルフケアDAY", items)
            },
            // パターン15: 目標設定 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("今月の目標を再確認", "月の後半に向けて、目標の進捗状況を見直した。"),
                    TestDiaryItem("計画の修正", "少し遅れ気味なので、週末に集中して作業する時間を確保する計画を立てた。"),
                    TestDiaryItem("モチベーション", "目標を達成した時のことを想像したら、やる気が湧いてきた。")
                )
                TestDiaryData("目標達成に向けた計画見直し", items)
            },
            // パターン16: ゲーム (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("新作RPGをプレイ", "発売日にダウンロードして早速プレイ開始。世界観が最高。"),
                    TestDiaryItem("最初のボス戦", "ギリギリの戦いだったけど、なんとか勝利。レベル上げが必要そうだ。")
                )
                TestDiaryData("ゲームの世界に没入", items)
            },
            // パターン17: 家族との時間 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("週末の家族ディナー", "みんなで食卓を囲んで食事。他愛もない話で笑い合えるのが幸せ。"),
                    TestDiaryItem("子供の成長", "子供が新しい言葉を覚えた。日々の成長を見るのが楽しみ。"),
                    TestDiaryItem("昔のアルバム", "古いアルバムを引っ張り出してきて、思い出話に花が咲いた。"),
                    TestDiaryItem("共同作業", "一緒に夕食の準備をした。共同作業も楽しいものだ。")
                )
                TestDiaryData("家族と過ごす温かい時間", items)
            },
            // パターン18: ネットサーフィン (項目数: 1)
            {
                val items = listOf(
                    TestDiaryItem("面白い記事を発見", "ネットニュースで興味深い科学記事を読んだ。知らない世界が広がって面白い。")
                )
                TestDiaryData("ネットの海を漂う一日", items)
            },
            // パターン19: 過去の思い出 (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("学生時代を思い出す", "ふとした瞬間に、高校時代の文化祭の準備で忙しかった日々を思い出した。"),
                    TestDiaryItem("昔の写真", "スマートフォンの写真フォルダを遡っていたら、旅行の写真が出てきて懐かしくなった。"),
                    TestDiaryItem("思い出の曲", "ラジオから流れてきた曲が、昔よく聴いていた曲で、一気に当時にタイムスリップした気分。"),
                    TestDiaryItem("卒業文集", "本棚の奥から卒業文集を発見。自分の書いた文章が若すぎて恥ずかしい。"),
                    TestDiaryItem("旧友への連絡", "懐かしくなって、何年も連絡を取っていなかった友人にメッセージを送ってみた。")
                )
                TestDiaryData("懐かしい思い出に浸る", items)
            },
            // パターン20: スキルアップ (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("オンライン講座を受講", "マーケティングに関するオンライン講座で新しい知識をインプット。"),
                    TestDiaryItem("学んだことの実践", "講座で学んだフレームワークを、早速仕事の資料作成に応用してみた。"),
                    TestDiaryItem("今後の課題", "知識を定着させるために、継続的なアウトプットが必要だと感じた。")
                )
                TestDiaryData("自己投資とスキルアップ", items)
            },
            // パターン21: ドライブ (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("海沿いをドライブ", "天気が良かったので、窓を全開にして海沿いの道を走った。潮風が気持ちいい。"),
                    TestDiaryItem("好きな音楽と共に", "お気に入りのプレイリストを大音量でかけながらの運転は最高だ。")
                )
                TestDiaryData("気ままなドライブ旅", items)
            },
            // パターン22: 家でのんびり (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("何もしない贅沢", "今日は予定を何も入れず、家でひたすらゴロゴロして過ごした。"),
                    TestDiaryItem("溜まっていたドラマを消化", "録画していた連続ドラマを1話から一気見。続きが気になる！"),
                    TestDiaryItem("お昼寝", "ソファでうとうとしていたら、いつの間にか2時間も寝てしまっていた。最高の休日。"),
                    TestDiaryItem("デリバリー", "夕食はデリバリーを頼んで、料理もサボることに決めた。")
                )
                TestDiaryData("最高の休日、おうち時間", items)
            },
            // パターン23: 新しい場所の開拓 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("一駅歩いてみた", "いつもは電車に乗る区間を、今日は歩いてみることに。"),
                    TestDiaryItem("知らないお店", "歩いている途中で、趣のある古本屋やおしゃれな雑貨屋を見つけた。"),
                    TestDiaryItem("新しい発見", "普段通らない道を歩くだけで、新しい発見がたくさんあって楽しかった。")
                )
                TestDiaryData("近所の新たな魅力探し", items)
            },
            // パターン24: ボランティア活動 (項目数: 2)
            {
                val items = listOf(
                    TestDiaryItem("地域の清掃活動に参加", "朝早くから、公園のゴミ拾いボランティアに参加した。"),
                    TestDiaryItem("活動後の達成感", "汗をかいた後の達成感は格別。街がきれいになって気持ちがいい。")
                )
                TestDiaryData("社会貢献でリフレッシュ", items)
            },
            // パターン25: 失敗談 (項目数: 5)
            {
                val items = listOf(
                    TestDiaryItem("寝坊して大慌て", "目覚ましをかけ忘れて、いつもより1時間も寝坊。朝からバタバタだった。"),
                    TestDiaryItem("大事なものを忘れる", "家を出る直前に、今日の会議で使う大事な資料を忘れたことに気づいた。"),
                    TestDiaryItem("今日の教訓", "前日の夜に、次の日の準備をしっかりしておくことの大切さを痛感した。"),
                    TestDiaryItem("電車を乗り間違える", "ぼーっとしていて、反対方向の電車に乗ってしまった。"),
                    TestDiaryItem("笑い話", "失敗続きの一日だったけど、後から考えれば笑い話になりそうだ。")
                )
                TestDiaryData("ちょっとツイてない一日", items)
            },
            // パターン26: 夢の話 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("不思議な夢を見た", "空を飛ぶ夢を見た。自由に飛び回れて、とても気持ちが良かった。"),
                    TestDiaryItem("夢の登場人物", "なぜか昔の同級生が出てきた。何か意味があるのだろうか。"),
                    TestDiaryItem("夢の記憶", "目が覚めた直後は覚えていたのに、段々と内容が薄れていくのがもどかしい。")
                )
                TestDiaryData("今日の夢日記", items)
            },
            // パターン27: アート鑑賞 (項目数: 1)
            {
                val items = listOf(
                    TestDiaryItem("美術館へ", "話題の現代アート展に行ってきた。作品の意図を考えるのが面白い。")
                )
                TestDiaryData("美術館でアートに触れる", items)
            },
            // パターン28: 目標達成 (項目数: 4)
            {
                val items = listOf(
                    TestDiaryItem("ついに目標達成！", "3ヶ月間続けてきた「毎日1万歩歩く」という目標をついに達成できた。"),
                    TestDiaryItem("達成感", "継続は力なり、という言葉を実感。自分に自信がついた。"),
                    TestDiaryItem("次の目標", "次は「週末に30分ジョギングする」という新しい目標を立ててみようと思う。"),
                    TestDiaryItem("ご褒美", "目標達成のご褒美に、少しリッチなディナーを予約した。")
                )
                TestDiaryData("継続の果てに掴んだ成功", items)
            },
            // パターン29: 音楽鑑賞 (項目数: 3)
            {
                val items = listOf(
                    TestDiaryItem("新しいアルバム", "好きなバンドの新しいアルバムを一日中リピートしていた。"),
                    TestDiaryItem("お気に入りの曲", "3曲目のメロディが特に好き。歌詞も心に沁みる。"),
                    TestDiaryItem("ライブに行きたい", "このアルバムの曲を生で聴いたら最高だろうな。次のツアーが待ち遠しい。")
                )
                TestDiaryData("最高の音楽に浸る一日", items)
            }
        )

        // 引数で渡されたパターン番号のラムダを実行してデータを返す
        // パターン番号が範囲外の場合は0番目を返す
        val index = if (patternNumber in diaryTemplates.indices) patternNumber else 0
        return diaryTemplates[index]()
    }
    //endregion
}
