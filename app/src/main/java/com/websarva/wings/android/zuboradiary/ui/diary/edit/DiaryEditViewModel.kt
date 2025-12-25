package com.websarva.wings.android.zuboradiary.ui.diary.edit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemNumber
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitleSelectionHistory
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.image.CacheDiaryImageUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.image.ClearDiaryImageCacheFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.weather.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySaveConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.weather.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.image.exception.DiaryImageCacheException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.image.exception.DiaryImageCacheFileClearException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByDateException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByIdException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadConfirmationCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiarySaveException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryUpdateConfirmationCheckException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.CheckWeatherInfoFetchEnabledUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.weather.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.weather.exception.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.ui.diary.common.mapper.toDomainModel
import com.websarva.wings.android.zuboradiary.ui.diary.common.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.common.model.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.ConditionUi
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.common.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.common.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.common.viewmodel.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.ui.diary.common.viewmodel.DiaryUiStateHelper
import com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle.DiaryItemTitleSelectionHistoryUi
import com.websarva.wings.android.zuboradiary.ui.diary.edit.itemtitle.DiaryItemTitleSelectionUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

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
    private val cacheDiaryImageUseCase: CacheDiaryImageUseCase,
    private val clearDiaryImageCacheFileUseCase: ClearDiaryImageCacheFileUseCase
) : BaseFragmentViewModel<
        DiaryEditUiState,
        DiaryEditUiEvent,
        DiaryEditNavDestination,
        DiaryEditNavBackDestination
        >(
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

        val args = DiaryEditFragmentArgs.fromSavedStateHandle(handle)
        val id = args.params.diaryId?.let { DiaryId(it) }
        val date = args.params.diaryDate
        launchWithUnexpectedErrorHandler(
            onError = {
                updateToDiaryPreparationErrorState()
                navigatePreviousScreen(date)
            }
        ) {
            prepareDiaryEntry(id, date)
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
            startPreviousScreenNavigationProcess(diary, originalDiary)
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
            startDiarySaveProcess(
                diary,
                diaryItemTitleSelectionHistoryList,
                originalDiary,
                isNewDiary
            )
        }
    }

    /**
     * 日記削除メニューがクリックされた時に呼び出される事を想定。
     * 日記の削除確認ダイアログを表示する。
     */
    internal fun onDiaryDeleteMenuClick() {
        if (!isReadyForOperation) return
        if (currentUiState.isNewDiary) return

        val originalDiaryId = DiaryId(originalDiary.id)
        val originalDiaryDate = originalDiary.date
        launchWithUnexpectedErrorHandler {
            showDiaryDeleteDialog(originalDiaryId, originalDiaryDate)
        }
    }

    /**
     * ナビゲーションアイコンがクリックされた時に呼び出される事を想定。
     * 前の画面へ遷移する処理を開始する。
     */
    fun onNavigationClick() {
        if (!isReadyForOperation) return

        val diary = currentUiState.editingDiary.toDomainModel()
        val originalDiary = originalDiary.toDomainModel()
        launchWithUnexpectedErrorHandler {
            startPreviousScreenNavigationProcess(diary, originalDiary)
        }
    }

    /**
     * 日付入力欄がクリックされた時に呼び出される事を想定。
     * 日付を選択するダイアログを表示する。
     */
    fun onDateInputFieldClick() {
        if (!isReadyForOperation) return

        val date = currentUiState.editingDiary.date
        val originalDate = originalDiary.date
        val isNewDiary = currentUiState.isNewDiary
        launchWithUnexpectedErrorHandler {
            showDatePickerDialog(date, originalDate, isNewDiary)
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
     * 日記項目タイトルを編集するダイアログを表示する。
     * @param itemNumberInt 対象の項目番号
     */
    fun onItemTitleInputFieldClick(itemNumberInt: Int) {
        if (!isReadyForOperation) return

        val itemNumber = DiaryItemNumber(itemNumberInt)
        val itemTitle =
            checkNotNull(currentUiState.editingDiary.itemTitles[itemNumberInt])
        launchWithUnexpectedErrorHandler {
            showDiaryItemTitleEditDialog(itemNumber, itemTitle)
        }
    }

    /**
     * 日記項目タイトル入力欄のテキストが変更された時に呼び出される事を想定。
     * 日記項目タイトルを更新する。
     * @param itemNumberInt 対象の項目番号
     * @param text 変更後のテキスト
     */
    fun onItemTitleTextChanged(itemNumberInt: Int, text: CharSequence) {
        val itemNumber = DiaryItemNumber(itemNumberInt)
        updateItemTitle(
            itemNumber,
            text.toString()
        )
    }

    /**
     * 日記項目追加ボタンがクリックされた時に呼び出される事を想定。
     * 日記項目入力欄の追加処理を開始する。
     */
    fun onItemAdditionButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            startDiaryItemAdditionAnimation()
        }
    }

    /**
     * 日記項目コメント入力欄のテキストが変更された時に呼び出される事を想定。
     * 日記項目コメントを更新する。
     * @param itemNumberInt 対象の項目番号
     * @param text 変更後のテキスト
     */
    fun onItemCommentTextChanged(itemNumberInt: Int, text: CharSequence) {
        val itemNumber = DiaryItemNumber(itemNumberInt)
        updateItemComment(
            itemNumber,
            text.toString()
        )
    }

    /**
     * 日記項目削除ボタンがクリックされた時に呼び出される事を想定。
     * 日記項目の削除確認ダイアログを表示する。
     */
    fun onItemDeleteButtonClick(itemNumberInt: Int) {
        if (!isReadyForOperation) return

        val itemNumber = DiaryItemNumber(itemNumberInt)
        launchWithUnexpectedErrorHandler {
            showDiaryItemDeleteDialog(itemNumber)
        }
    }

    /**
     * 添付画像削除ボタンがクリックされた時に呼び出される事を想定。
     * 添付画像の削除確認ダイアログを表示する。
     */
    fun onAttachedImageDeleteButtonClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            showDiaryImageDeleteDialog()
        }
    }

    /**
     * 添付画像欄がクリックされた時に呼び出される事を想定。
     * 添付する画像を選択するギャラリーを表示する。
     */
    fun onAttachedImageClick() {
        if (!isReadyForOperation) return

        launchWithUnexpectedErrorHandler {
            showImageSelectionGallery()
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
     * 日記項目データを追加する。
     * @param itemNumberInt 対象の項目番号
     * */
    internal fun onDiaryItemVisibleStateTransitionCompleted(itemNumberInt: Int) {
        val itemNumber = DiaryItemNumber(itemNumberInt)
        addItem(itemNumber)
        updateToIdleState()
    }
    //endregion

    //region UI Event Handlers - Results

    //region Diary Load Dialog Results
    /**
     * 日記読み込み確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 日記データを読み込む。
     */
    internal fun onDiaryLoadDialogPositiveResultReceived() {
        val parameters = checkNotNull(pendingDiaryLoadParameters)
        clearPendingDiaryLoadParameters()
        launchWithUnexpectedErrorHandler {
            loadDiaryByDate(parameters.date)
        }
    }

    /**
     * 日記読み込み確認ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 天気情報を取得する。
     */
    internal fun onDiaryLoadDialogNegativeResultReceived() {
        val parameters = checkNotNull(pendingDiaryLoadParameters)
        clearPendingDiaryLoadParameters()
        launchWithUnexpectedErrorHandler {
            startWeatherInfoFetchProcess(parameters.date, parameters.previousDate)
        }
    }
    //endregion

    //region Diary Update Dialog Results
    /**
     * 日記上書き確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 日記を保存する。
     */
    internal fun onDiaryUpdateDialogPositiveResultReceived() {
        val parameters = checkNotNull(pendingDiaryUpdateParameters)
        clearPendingDiaryUpdateParameters()
        launchWithUnexpectedErrorHandler {
            saveDiary(
                parameters.diary,
                parameters.diaryItemTitleSelectionHistoryList,
                parameters.originalDiary,
                parameters.isNewDiary
            )
        }
    }

    /**
     * 日記上書き確認ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 日記更新パラメータ（[pendingDiaryUpdateParameters]）をクリアする。
     */
    internal fun onDiaryUpdateDialogNegativeResultReceived() {
        clearPendingDiaryUpdateParameters()
    }
    //endregion

    //region Diary Delete Dialog Results
    /**
     * 日記削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 日記を削除する。
     */
    internal fun onDiaryDeleteDialogPositiveResultReceived() {
        val parameters = checkNotNull(pendingDiaryDeleteParameters)
        clearPendingDiaryDeleteParameters()
        launchWithUnexpectedErrorHandler {
            deleteDiary(parameters.id, parameters.date)
        }
    }

    /**
     * 日記削除確認ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 日記削除パレメータ（[pendingDiaryDeleteParameters]）をクリアする。
     */
    internal fun onDiaryDeleteDialogNegativeResultReceived() {
        clearPendingDiaryDeleteParameters()
    }
    //endregion

    //region Date Picker Dialog Results
    /**
     * 日付選択ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 日記の日付を変更する。
     * @param date 選択された新しい日付
     */
    internal fun onDatePickerDialogPositiveResultReceived(date: LocalDate) {
        val parameters = checkNotNull(pendingDiaryDateUpdateParameters)
        clearPendingDiaryDateUpdateParameters()
        launchWithUnexpectedErrorHandler {
            handleDatePickcerDialogResult(date, parameters.originalDate, parameters.isNewDiary)
        }
    }

    /**
     * 日付選択ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 日記日付更新パラメータ（[pendingDiaryDateUpdateParameters]）をクリアする。
     */
    internal fun onDatePickerDialogNegativeResultReceived() {
        clearPendingDiaryDateUpdateParameters()
    }
    //endregion

    //region Diary Load Failure Dialog Results
    /**
     * 日記読み込み失敗ダイアログから結果を受け取った時に呼び出される事を想定。
     * 前の画面へ遷移する（イベント発行）。
     */
    internal fun onDiaryLoadFailureDialogResultReceived() {
        launchWithUnexpectedErrorHandler {
            navigatePreviousScreenOnInitialDiaryLoadFailed()
        }
    }
    //endregion

    //region Weather Info Fetch Dialog Results
    /**
     * 天気情報取得確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 権限を確認する。
     */
    internal fun onWeatherInfoFetchDialogPositiveResultReceived() {
        launchWithUnexpectedErrorHandler {
            checkLocationPermissionBeforeWeatherInfoFetch()
        }
    }

    /**
     * 天気情報取得確認ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 天気情報取得パラメータ（[pendingWeatherInfoFetchParameters]）をクリアする。
     */
    internal fun onWeatherInfoFetchDialogNegativeResultReceived() {
        clearPendingWeatherInfoFetchParameters()
    }
    //endregion

    //region Diary Item Delete Dialog Results
    /**
     * 日記項目削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 日記項目削除アニメーションを開始する。
     */
    internal fun onDiaryItemDeleteDialogPositiveResultReceived() {
        val parameters = checkNotNull(pendingDiaryItemDeleteParameters)
        clearPendingDiaryItemDeleteParameters()
        launchWithUnexpectedErrorHandler {
            startDiaryItemDeleteAnimation(parameters.itemNumber)
        }
    }

    /**
     * 日記項目削除確認ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 日記項目削除パラメータ（[pendingDiaryItemDeleteParameters]）をクリアする。
     */
    internal fun onDiaryItemDeleteDialogNegativeResultReceived() {
        clearPendingDiaryItemDeleteParameters()
    }
    //endregion

    //region Diary Image Delete Dialog Results
    /**
     * 添付画像削除確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 添付画像を削除する。
     */
    internal fun onDiaryImageDeleteDialogPositiveResultReceived() {
        launchWithUnexpectedErrorHandler {
            deleteImage()
        }
    }
    //endregion

    //region Exit Without Diary Save Dialog Results
    /**
     * 未保存終了確認ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 前の画面へ遷移する。
     */
    internal fun onExitWithoutDiarySaveDialogPositiveResultReceived() {
        val parameters = checkNotNull(pendingPreviousNavigationParameters)
        clearPendingPreviousNavigationParameters()
        launchWithUnexpectedErrorHandler {
            navigatePreviousScreen(parameters.originalDiaryDate)
        }
    }

    /**
     * 未保存終了確認ダイアログからNegative結果を受け取った時に呼び出される事を想定。
     * 前の画面へ渡すパラメータ（[pendingPreviousNavigationParameters]）をクリアする。
     */
    internal fun onExitWithoutDiarySaveDialogNegativeResultReceived() {
        clearPendingPreviousNavigationParameters()
    }
    //endregion

    //region Item Title Edit Dialog Results
    /**
     * 日記項目タイトル編集ダイアログからPositive結果を受け取った時に呼び出される事を想定。
     * 項目タイトルを更新する。
     * @param selection 選択されたタイトル情報
     */
    internal fun onItemTitleEditDialogPositiveResultReceived(selection: DiaryItemTitleSelectionUi) {
        updateItemTitle(selection)
    }
    //endregion

    //region Open Document Results
    /**
     * ギャラリーから画像を選択した結果を受け取った時に呼び出される事を想定。
     * 選択された画像をキャッシュする。
     * @param uri 選択された画像のURI（未選択の場合はnull）
     */
    internal fun onOpenDocumentImageUriResultReceived(uri: Uri?) {
        launchWithUnexpectedErrorHandler {
            cacheDiaryImage(uri)
        }
    }
    //endregion

    //endregion

    //region UI Event Handlers - Permissions
    /**
     * 位置情報権限の確認結果を受け取った時に呼び出される事を想定。
     * 権限が許可されている場合のみ、天気情報の取得を実行する。
     * @param isGranted 権限が許可されている場合はtrue
     */
    internal fun onAccessLocationPermissionChecked(
        isGranted: Boolean
    ) {
        val parameters = checkNotNull(pendingWeatherInfoFetchParameters)
        clearPendingWeatherInfoFetchParameters()
        launchWithUnexpectedErrorHandler {
            executeWeatherInfoFetchWithPermissionCheck(
                isGranted,
                parameters.date
            )
        }
    }
    //endregion

    //region Business Logic

    //region Diary Operation
    /**
     * 日記エントリの準備を行う。IDの有無で新規作成か既存の読み込みかを判断する。
     * @param id 既存の日記ID（新規の場合はnull）
     * @param date 対象の日付
     */
    private suspend fun prepareDiaryEntry(id: DiaryId?, date: LocalDate) {
        if (id == null) {
            prepareNewDiaryEntry(date)
        } else {
            prepareExsistsDiaryEntry(id, date)
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
        startDiaryCheckProcessBeforeEditing(
            date,
            previousDate,
            originalDate,
            isNewDiary
        )
    }

    /**
     * 既存日記エントリの準備を行う。
     * @param id 既存の日記ID
     * @param date 対象の日付
     */
    private suspend fun prepareExsistsDiaryEntry(id: DiaryId, date: LocalDate) {
        loadDiaryById(id, date)
    }

    /**
     * 新規作成編集準備後、編集日記日付変更後の日記状態確認プロセスを開始する。(既存日記編集準備ではこのプロセスは不要)
     * 日記の日付に対して既存日記が存在する場合は、
     * 日記の状態をもとに既存日記の読み込みを確認するダイアログを表示するかどうかを判断し、
     * 必要に応じてダイアログを表示する（イベント発行）。
     * 表示不要の場合は天気情報の取得処理を開始する。
     * @param date 編集日記の日付
     * @param previousDate 以前選択されていた日付
     * @param originalDate 元の日記の日付
     * @param isNewDiary 新規日記の場合はtrue
     */
    private suspend fun startDiaryCheckProcessBeforeEditing(
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
                    cachePendingDiaryLoadParameters(date, previousDate)
                    emitNavigationEvent(
                        NavigationEvent.To(
                            DiaryEditNavDestination.DiaryLoadDialog(date),
                            NavigationEvent.Policy.Retry
                        )
                    )
                } else {
                    startWeatherInfoFetchProcess(date, previousDate)
                }
            }
            is UseCaseResult.Failure -> {
                updateToIdleState()
                when (result.exception) {
                    is DiaryLoadConfirmationCheckException.CheckFailure -> {
                        showAppMessageDialog(DiaryEditAppMessage.DiaryInfoLoadFailure)
                    }
                    is DiaryLoadConfirmationCheckException.Unknown -> {
                        showUnexpectedAppMessageDialog(result.exception)
                    }
                }
            }
        }
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
                requireNotNull(id)
                loadDiaryByIdUseCase(id)
            },
            { exception ->
                when (exception) {
                    is DiaryLoadByIdException.LoadFailure -> {
                        showAppMessageDialog(DiaryEditAppMessage.DiaryLoadFailure)
                    }
                    is DiaryLoadByIdException.Unknown -> {
                        showUnexpectedAppMessageDialog(exception)
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
                        showAppMessageDialog(DiaryEditAppMessage.DiaryLoadFailure)
                    }
                    is DiaryLoadByDateException.Unknown -> {
                        showUnexpectedAppMessageDialog(exception)
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
                    updateToDiaryPreparationErrorState()

                    // MEMO:連続するUIイベント（エラー表示と画面遷移）は、監視開始前に発行されると
                    //      取りこぼされる可能性がある。これを防ぐため、間に確認ダイアログを挟み、
                    //      ユーザーの応答を待ってから画面遷移を実行する。
                    emitNavigationEvent(
                        NavigationEvent.To(
                            DiaryEditNavDestination.DiaryLoadFailureDialog(date),
                            NavigationEvent.Policy.Retry
                        )
                    )
                } else {
                    updateUiState { previousState }
                    emitAppMessageOnFailure(result.exception)
                }
            }
        }
    }

    /**
     * 日記の保存プロゼスを開始する。
     * 日記の上書き保存を確認するダイアログを表示するかどうかを判断し、必要に応じてダイアログを表示する（イベント発行）。
     * 表示不要な場合はそのまま保存する。
     * @param diary 保存する日記データ
     * @param diaryItemTitleSelectionHistoryList 項目タイトル選択履歴のリスト
     * @param originalDiary 保存前の元の日記データ
     * @param isNewDiary 新規日記の場合はtrue
     */
    private suspend fun startDiarySaveProcess(
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
                    cachePendingDiaryUpdateParameters(
                        diary,
                        diaryItemTitleSelectionHistoryList,
                        originalDiary,
                        isNewDiary
                    )
                    emitNavigationEvent(
                        NavigationEvent.To(
                            DiaryEditNavDestination.DiaryUpdateDialog(diary.date),
                            NavigationEvent.Policy.Retry
                        )
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
                        showAppMessageDialog(
                            DiaryEditAppMessage.DiarySaveFailure
                        )
                    }
                    is DiaryUpdateConfirmationCheckException.Unknown -> {
                        showUnexpectedAppMessageDialog(result.exception)
                    }
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
                emitNavigationEvent(
                    NavigationEvent.To(
                        DiaryEditNavDestination
                            .DiaryShowScreen(diary.id.value, diary.date),
                        NavigationEvent.Policy.Retry
                    )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateToIdleState()
                when (result.exception) {
                    is DiarySaveException.SaveFailure -> {
                        showAppMessageDialog(
                            DiaryEditAppMessage.DiarySaveFailure
                        )
                    }
                    is DiarySaveException.InsufficientStorage -> {
                        showAppMessageDialog(
                            DiaryEditAppMessage.DiarySaveInsufficientStorageFailure
                        )
                    }
                    is DiarySaveException.Unknown -> showUnexpectedAppMessageDialog(result.exception)
                }
            }
        }
    }

    /**
     * 日記の削除確認ダイアログを表示する。
     * 渡されたパラメータをキャッシュし、ダイアログを表示する（イベント発行）。
     *
     * @param diaryId 削除対象の日記のID。
     * @param date 削除対象の日記の日付。
     */
    private suspend fun showDiaryDeleteDialog(diaryId: DiaryId, date: LocalDate) {
        cachePendingDiaryDeleteParameters(
            diaryId,
            date
        )
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryEditNavDestination.DiaryDeleteDialog(date),
                NavigationEvent.Policy.Single
            )
        )
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
                emitNavigationEvent(
                    NavigationEvent.Back(
                        NavigationEvent.Policy.Retry,
                        date,
                        DiaryEditNavBackDestination.ExitDiaryFlow
                    )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗")
                updateToIdleState()
                when (result.exception) {
                    is DiaryDeleteException.DiaryDataDeleteFailure -> {
                        showAppMessageDialog(DiaryEditAppMessage.DiaryDeleteFailure)
                    }
                    is DiaryDeleteException.ImageFileDeleteFailure -> {
                        showAppMessageDialog(DiaryEditAppMessage.DiaryImageDeleteFailure)
                    }
                    is DiaryDeleteException.Unknown -> showUnexpectedAppMessageDialog(result.exception)
                }
            }
        }
    }
    //endregion

    //region Diary Date Operation
    /**
     * 日付選択ダイアログを表示する。
     * 渡されたパラメータをキャッシュし、ダイアログを表示する（イベント発行）。
     *
     * @param currentDate 現在選択されている日付。
     * @param originalDate 元の日付。
     * @param isNewDiary 新規日記かどうか。
     */
    private suspend fun showDatePickerDialog(
        currentDate: LocalDate,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        cachePendingDiaryDateUpdateParameters(originalDate, isNewDiary)
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryEditNavDestination.DatePickerDialog(currentDate),
                NavigationEvent.Policy.Single
            )
        )
    }

    /**
     * 変更された日記の日付を更新し、必要に応じて既存の日記の読み込み確認を行う。
     * @param date 新しい日付
     * @param originalDate 元の日付
     * @param isNewDiary 新規日記の場合はtrue
     */
    private suspend fun handleDatePickcerDialogResult(
        date: LocalDate,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ) {
        updateDate(date)
        val previousDate = currentUiState.previousSelectedDate
        // MEMO:下記処理をdate(StateFlow)変数のCollectorから呼び出すと、
        //      画面回転時にも不要に呼び出してしまう為、下記にて処理。
        startDiaryCheckProcessBeforeEditing(
            date,
            previousDate,
            originalDate,
            isNewDiary
        )
    }
    //endregion

    //region Diary Item Operation

    /**
     * 日記項目タイトルの編集ダイアログを表示する（イベント発行）。
     *
     * @param itemNumber 編集対象の項目番号。
     * @param currentTitle 現在の項目タイトル。
     */
    private suspend fun showDiaryItemTitleEditDialog(itemNumber: DiaryItemNumber, currentTitle: String) {
        // MEMO:日記項目タイトルIDは受取用でここでは不要の為、nullとする。
        val itemTitleId = null
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryEditNavDestination.DiaryItemTitleEditDialog(
                    DiaryItemTitleSelectionUi(itemNumber.value, itemTitleId, currentTitle)
                ),
                NavigationEvent.Policy.Single
            )
        )
    }

    /** 日記項目の追加アニメーションのイベントを発行する。 */
    private suspend fun startDiaryItemAdditionAnimation() {
        updateToInputDisabledState()
        val numVisibleItems = currentUiState.numVisibleDiaryItems
        val additionItemNumber = numVisibleItems + 1
        emitUiEvent(DiaryEditUiEvent.startDiaryItemAdditionAnimation(additionItemNumber))
    }

    /**
     * 日記項目データを追加する。
     * @param itemNumber 削除する項目の番号
     */
    private fun addItem(itemNumber: DiaryItemNumber) {
        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(
                    itemTitles = it.editingDiary.itemTitles + (itemNumber.value to ""),
                    itemComments = it.editingDiary.itemComments + (itemNumber.value to "")
                )
            )
        }
    }

    /**
     * 日記項目の削除確認ダイアログを表示する。
     * 渡されたパラメータをキャッシュし、ダイアログを表示する（イベント発行）。
     *
     * @param itemNumber 削除対象の項目番号。
     */
    private suspend fun showDiaryItemDeleteDialog(itemNumber: DiaryItemNumber) {
        cachePendingDiaryItemDeleteParameters(itemNumber)
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryEditNavDestination.DiaryItemDeleteDialog(itemNumber.value),
                NavigationEvent.Policy.Single
            )
        )
    }

    /**
     * 日記項目の削除アニメーションの開始する（イベント発行）。
     * 削除対象が項目1で、現在表示されている項目が項目1のみの場合、アニメーションを開始せずにデータのみを削除する。
     * @param itemNumber 削除する項目の番号
     */
    // MEMO:日記項目削除処理完了時のUi更新(編集中)は日記項目削除メソッドにて処理
    private suspend fun startDiaryItemDeleteAnimation(itemNumber: DiaryItemNumber) {
        val numVisibleItems = currentUiState.numVisibleDiaryItems

        updateToInputDisabledState()
        if (itemNumber.isMinNumber && itemNumber.value == numVisibleItems) {
            deleteItem(itemNumber)
        } else {
            emitUiEvent(
                DiaryEditUiEvent.startDiaryItemDeleteAnimation(itemNumber.value)
            )
        }
    }

    /**
     * 日記項目データを削除し、後続の項目データを繰り上げる。
     * （例：項目2を削除した場合、項目3～5のデータを項目2～4へ繰り上げる）
     * @param itemNumber 削除する項目の番号
     */
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
    //endregion

    //region Diary Image Operation
    /**
     * 画像を選択するギャラリーを表示する（イベント発行）。
     */
    // MEMO:画像選択完了時のUi更新(編集中)は画像選択完了イベントメソッドにて処理
    private suspend fun showImageSelectionGallery() {
        emitUiEvent(DiaryEditUiEvent.ShowImageSelectionGallery)
    }

    /**
     * 選択された画像をキャッシュし、ファイル名をUI状態に保存する。
     * @param uri 選択された画像のURI
     */
    private suspend fun cacheDiaryImage(uri: Uri?) {
        updateToProcessingState()
        if (uri != null) {
            when (val result = cacheDiaryImageUseCase(uri.toString())) {
                is UseCaseResult.Success -> {
                    updateImageFileName(result.value.fullName)
                }
                is UseCaseResult.Failure -> {
                    when (result.exception) {
                        is DiaryImageCacheException.CacheFailure -> {
                            showAppMessageDialog(
                                DiaryEditAppMessage.ImageLoadFailure
                            )
                        }
                        is DiaryImageCacheException.InsufficientStorage -> {
                            showAppMessageDialog(
                                DiaryEditAppMessage.ImageLoadInsufficientStorageFailure
                            )
                        }
                        is DiaryImageCacheException.Unknown -> {
                            showUnexpectedAppMessageDialog(result.exception)
                        }
                    }
                }
            }
        }
        updateToIdleState()
    }

    /** 添付画像の削除確認ダイアログを表示する（イベント発行）。 */
    private suspend fun showDiaryImageDeleteDialog() {
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryEditNavDestination.DiaryImageDeleteDialog,
                NavigationEvent.Policy.Single
            )
        )
    }

    /** 添付画像を削除（UI状態の更新とキャッシュファイルを削除）する。 */
    private suspend fun deleteImage() {
        updateImageFileName(null)
        clearDiaryImageCacheFile()
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
                        showUnexpectedAppMessageDialog(result.exception)
                    }
                }
            }
        }
    }
    //endregion

    //region Weather Info Fetch
    /**
     * 天気情報の取得プロセスを開始する。
     * 天気情報取得の確認ダイアログを表示するか判断し、必要に応じてダイアログを表示する（イベント発行）。
     * 表示が不要な場合は、位置情報権限を確認する（イベント発行）。
     * 天気情取得設定が無効の場合は何もしない。
     * @param date 天気情報を取得する日付
     * @param previousDate 以前選択されていた日付
     */
    private suspend fun startWeatherInfoFetchProcess(
        date: LocalDate,
        previousDate: LocalDate?
    ) {
        val isEnabled = checkWeatherInfoFetchEnabledUseCase().value
        if (!isEnabled) return

        val shouldRequest =
            shouldRequestWeatherInfoConfirmationUseCase(date, previousDate).value
        if (shouldRequest) {
            cachePendingWeatherInfoFetchParameters(date)
            emitNavigationEvent(
                NavigationEvent.To(
                    DiaryEditNavDestination.WeatherInfoFetchDialog(date),
                    NavigationEvent.Policy.Retry
                )
            )
        } else {
            val shouldLoad = shouldFetchWeatherInfoUseCase(date, previousDate).value
            if (!shouldLoad) return

            cachePendingWeatherInfoFetchParameters(date)
            checkLocationPermissionBeforeWeatherInfoFetch()
        }
    }

    /** 天気情報取得前の位置情報権限の確認を行う（イベント発行）。 */
    private suspend fun checkLocationPermissionBeforeWeatherInfoFetch() {
        emitUiEvent(
            DiaryEditUiEvent.CheckAccessLocationPermissionBeforeWeatherInfoFetch
        )
    }

    /**
     * 位置情報権限の有無を確認し、許可されている場合に天気情報の取得を実行する。
     * 権限がない場合は、ユーザーに権限が必要である旨をダイアログで表示する（イベント発行）。
     * @param isGranted 位置情報権限が許可されているか
     * @param date 天気情報を取得する日付
     */
    private suspend fun executeWeatherInfoFetchWithPermissionCheck(
        isGranted: Boolean,
        date: LocalDate
    ) {
        if (!isGranted) {
            updateToIdleState()
            showAppMessageDialog(DiaryEditAppMessage.AccessLocationPermissionRequest)
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
                        showAppMessageDialog(
                            DiaryEditAppMessage.AccessLocationPermissionRequest
                        )
                    }
                    is WeatherInfoFetchException.DateOutOfRange -> {
                        showAppMessageDialog(DiaryEditAppMessage.WeatherInfoDateOutOfRange)
                    }
                    is WeatherInfoFetchException.LocationAccessFailure,
                    is WeatherInfoFetchException.FetchFailure -> {
                        showAppMessageDialog(DiaryEditAppMessage.WeatherInfoFetchFailure)
                    }
                    is WeatherInfoFetchException.Unknown -> showUnexpectedAppMessageDialog(result.exception)
                }
            }
        }
    }
    //endregion

    //region Navigation
    /**
     * 前の画面へ遷移する処理を開始する。変更がある場合は確認ダイアログを表示する（イベント発行）。
     * @param diary 現在の編集中の日記データ
     * @param originalDiary 元の日記データ
     */
    private suspend fun startPreviousScreenNavigationProcess(
        diary: Diary,
        originalDiary: Diary
    ) {
        val shouldRequest = shouldRequestExitWithoutDiarySaveConfirmationUseCase(
            diary,
            originalDiary
        ).value
        if (shouldRequest) {
            cachePendingPreviousNavigationParameter(originalDiary.date)
            emitNavigationEvent(
                NavigationEvent.To(
                    DiaryEditNavDestination.ExitWithoutDiarySaveDialog,
                    NavigationEvent.Policy.Single
                )
            )
        } else {
            navigatePreviousScreen(originalDiary.date)
        }
    }

    /**
     * 前の画面へ遷移する。
     * 日記画像のキャッシュファイルをクリアした後、画面遷移する（イベント発行）。
     * @param originalDiaryDate 遷移元に返す日付
     */
    private suspend fun navigatePreviousScreen(originalDiaryDate: LocalDate) {
        clearDiaryImageCacheFile()
        emitNavigationEvent(
            NavigationEvent.Back(
                NavigationEvent.Policy.Retry,
                        originalDiaryDate
            )
        )
    }

    /**
     * 前の画面へ遷移する（イベント発行）。
     * 初回の日記読み込み失敗時に呼び出す。
     */
    private suspend fun navigatePreviousScreenOnInitialDiaryLoadFailed() {
        emitNavigationEvent(
            NavigationEvent.Back(
                NavigationEvent.Policy.Retry,
                null
            )
        )
    }

    /**
     * アプリケーションメッセージダイアログを表示する（イベント発行）。
     * @param appMessage 表示するメッセージ。
     */
    private suspend fun showAppMessageDialog(appMessage: DiaryEditAppMessage) {
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryEditNavDestination.AppMessageDialog(appMessage),
                NavigationEvent.Policy.Retry
            )
        )
    }

    override suspend fun showUnexpectedAppMessageDialog(e: Exception) {
        showAppMessageDialog(DiaryEditAppMessage.Unexpected(e))
    }
    //endregion

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
     *
     * 対象日記項目が編集無効（追加されてない）状態の場合は更新されずに処理が終了する。
     *
     * @param itemNumber 対象の項目番号
     * @param title 新しいタイトル
     */
    private fun updateItemTitle(itemNumber: DiaryItemNumber, title: String) {
        // MEMO:日記項目削除でnullを代入してもEditTextViewが空文字としてリスナ通知されるため、下記ガード節で対応
        if (currentUiState.editingDiary.itemTitles[itemNumber.value] == null) return

        updateUiState {
            it.copy(
                editingDiary =
                    it.editingDiary.copy(
                        itemTitles = it.editingDiary.itemTitles + (itemNumber.value to title)
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
        if (currentUiState.editingDiary.itemTitles[itemNumberInt] == null)
            throw IllegalArgumentException("対象日記項目無効状態")

        val id = requireNotNull(selection.id)
        val title = selection.title
        val updateHistory = DiaryItemTitleSelectionHistoryUi(
            id,
            title,
            LocalDateTime.now()
        )
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
     * @param itemNumber 対象の項目番号
     * @param comment 新しいコメント
     */
    private fun updateItemComment(
        itemNumber: DiaryItemNumber,
        comment: String
    ) {
        // MEMO:日記項目削除でnullを代入してもEditTextViewが空文字としてリスナ通知されるため、下記ガード節で対応
        if (currentUiState.editingDiary.itemComments[itemNumber.value] == null) return

        updateUiState {
            it.copy(
                editingDiary = it.editingDiary.copy(
                    itemComments = it.editingDiary.itemComments + (itemNumber.value to comment)
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

    /** UIを日記編集準備失敗の状態に更新する。 */
    private fun updateToDiaryPreparationErrorState() {
        updateUiState {
            it.copy(
                originalDiaryLoadState = LoadState.Error,
                isProcessing = false,
                isInputDisabled = true
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

    //region Pending Operation Parameters

    //region Diary Load Parameters
    /**
     * 保留中の日記読み込みパラメータを更新する。
     * @param date 読み込み対象の日付
     * @param previousDate 以前選択されていた日付
     */
    private fun cachePendingDiaryLoadParameters(date: LocalDate, previousDate: LocalDate?) {
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

    //region Diary Update Parameters
    /**
     * 保留中の日記更新パラメータを更新する。
     * @param diary 保存する日記データ
     * @param diaryItemTitleSelectionHistoryList 項目タイトル選択履歴のリスト
     * @param originalDiary 保存前の元の日記データ
     * @param isNewDiary 新規日記の場合はtrue
     */
    private fun cachePendingDiaryUpdateParameters(
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

    //region Diary Delete Parameters
    /**
     * 保留中の日記削除パラメータを更新する。
     * @param id 削除対象の日記ID
     * @param date 削除対象の日記の日付
     */
    private fun cachePendingDiaryDeleteParameters(id: DiaryId, date: LocalDate) {
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

    //region Diary Date Update Parameters
    /**
     * 保留中の日記日付更新パラメータを更新する。
     * @param originalDate 元の日付
     * @param isNewDiary 新規日記の場合はtrue
     */
    private fun cachePendingDiaryDateUpdateParameters(
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

    //region Previous Navigation Parameters
    /**
     * 保留中の前画面遷移パラメータを更新する。
     * @param originalDiaryDate 元の日記の日付
     */
    private fun cachePendingPreviousNavigationParameter(originalDiaryDate: LocalDate) {
        pendingPreviousNavigationParameters = PreviousNavigationParameters(originalDiaryDate)
    }

    /** 保留中の前画面遷移パラメータをクリアする。 */
    private fun clearPendingPreviousNavigationParameters() {
        pendingPreviousNavigationParameters = null
    }

    /**
     * 前画面遷移処理に必要なパラメータを保持するデータクラス。
     * @property originalDiaryDate 元の日記の日付
     */
    private data class PreviousNavigationParameters(
        val originalDiaryDate: LocalDate
    )
    //endregion

    //region Weather Info Fetch Parameters
    /**
     * 保留中の天気情報取得パラメータを更新する。
     * @param date 取得対象の日付
     */
    private fun cachePendingWeatherInfoFetchParameters(date: LocalDate) {
        pendingWeatherInfoFetchParameters = WeatherInfoFetchParameters(date)
    }

    /** 保留中の天気情報取得パラメータをクリアする。 */
    private fun clearPendingWeatherInfoFetchParameters() {
        pendingWeatherInfoFetchParameters = null
    }

    /**
     * 天気情報取得処理に必要なパラメータを保持するデータクラス。
     * @property date 取得対象の日付
     */
    private data class WeatherInfoFetchParameters(
        val date: LocalDate
    )
    //endregion

    //region Diary Item Delete Parameters
    /**
     * 保留中の日記項目削除パラメータを更新する。
     * @param itemNumber 削除する項目の番号
     */
    private fun cachePendingDiaryItemDeleteParameters(itemNumber: DiaryItemNumber) {
        pendingDiaryItemDeleteParameters = DiaryItemDeleteParameters(itemNumber)
    }

    /** 保留中の日記項目削除パラメータをクリアする。 */
    private fun clearPendingDiaryItemDeleteParameters() {
        pendingDiaryItemDeleteParameters = null
    }

    /**
     * 日記項目削除処理に必要なパラメータを保持するデータクラス。
     * @property itemNumber 削除する項目の番号
     */
    private data class DiaryItemDeleteParameters(
        val itemNumber: DiaryItemNumber
    )
    //endregion

    //endregion

    private companion object {
        /** SavedStateHandleにUI状態を保存するためのキー。 */
        const val SAVED_STATE_UI_KEY = "saved_state_ui"
    }
}
