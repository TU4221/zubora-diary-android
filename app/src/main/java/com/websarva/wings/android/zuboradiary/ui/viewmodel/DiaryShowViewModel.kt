package com.websarva.wings.android.zuboradiary.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryDeleteException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception.DiaryLoadByIdException
import com.websarva.wings.android.zuboradiary.ui.model.message.DiaryShowAppMessage
import com.websarva.wings.android.zuboradiary.ui.model.event.DiaryShowUiEvent
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryShowFragmentArgs
import com.websarva.wings.android.zuboradiary.ui.mapper.toUiModel
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.navigation.event.NavigationEvent
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.DiaryShowNavDestination
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.DummyNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.model.state.ui.DiaryShowUiState
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.BaseFragmentViewModel
import com.websarva.wings.android.zuboradiary.ui.viewmodel.common.DiaryUiStateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

/**
 * 日記表示画面のUIロジックと状態([DiaryShowUiState])管理を担うViewModel。
 *
 * 以下の責務を持つ:
 * - [SavedStateHandle]から受け取ったIDに基づき、特定の日記データを読み込む
 * - ユーザー操作（編集、削除メニューなど）に応じたイベント処理と画面遷移の発行
 * - 日記の読み込み失敗や削除処理の結果をハンドリングし、適切なUIフィードバックを行う
 */
@HiltViewModel
class DiaryShowViewModel @Inject internal constructor(
    private val handle: SavedStateHandle,
    private val diaryUiStateHelper: DiaryUiStateHelper,
    private val loadDiaryByIdUseCase: LoadDiaryByIdUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase
) : BaseFragmentViewModel<
        DiaryShowUiState,
        DiaryShowUiEvent,
        DiaryShowNavDestination,
        DummyNavBackDestination
>(
    DiaryShowUiState()
) {

    //region Properties
    /** このViewModelが操作を受け入れ可能な状態かを示す。 */
    override val isReadyForOperation
        get() = !currentUiState.isInputDisabled
                && (currentUiState.diaryLoadState is LoadState.Success)

    /** UI状態から正常に読み込まれた日記データのみを抽出して提供するFlow。 */
    private val diaryFlow =
        uiState.distinctUntilChanged { old, new ->
            old.diaryLoadState == new.diaryLoadState
        }.mapNotNull { (it.diaryLoadState as? LoadState.Success)?.data }

    /** 読み込んだ日記データへのアクセスを提供する。 */
    private val diary
        get() = (currentUiState.diaryLoadState as LoadState.Success).data

    /** 削除処理が保留中であることを示すためのパラメータキャッシュ。 */
    private var pendingDiaryDeleteParameters: DiaryDeleteParameters? = null
    //endregion

    //region Initialization
    init {
        initializeDiaryData()
        collectUiStates()
    }

    /** [SavedStateHandle]からIDと日付を取得し、日記データの初期化を開始する。 */
    private fun initializeDiaryData() {
        val args = DiaryShowFragmentArgs.fromSavedStateHandle(handle)
        val parameters = args.diaryShowScreenParameters
        val id = DiaryId(checkNotNull(parameters.diaryId))
        val date = parameters.diaryDate
        launchWithUnexpectedErrorHandler {
            loadDiary(id, date)
        }
    }

    /** UI状態の監視を開始する。 */
    private fun collectUiStates() {
        collectWeather2Visible()
        collectNumVisibleDiaryItems()
        collectImageFilePath()
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

    /** 表示する日記項目数の変更を監視し、UIに反映させる。 */
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

        val date = diary.date
        launchWithUnexpectedErrorHandler {
            navigatePreviousScreen(date)
        }
    }

    /**
     * 編集メニューがクリックされた時に呼び出される事を想定。
     * 日記編集画面への遷移する。
     */
    internal fun onDiaryEditMenuClick() {
        if (!isReadyForOperation) return

        val id = diary.id
        val date = diary.date
        launchWithUnexpectedErrorHandler {
            navigateDiaryEditScreen(id, date)
        }
    }

    /**
     * 削除メニューがクリックされた時に呼び出される事を想定。
     * 日記の削除ダイアログを表示する。
     */
    internal fun onDiaryDeleteMenuClick() {
        if (!isReadyForOperation) return

        val id = diary.id
        val date = diary.date
        launchWithUnexpectedErrorHandler {
            showDiaryDeleteDialog(DiaryId(id), date)
        }
    }

    /**
     * ナビゲーションアイコンがクリックされた時に呼び出される事を想定。
     * 前の画面へ遷移する。
     */
    fun onNavigationIconClick() {
        if (!isReadyForOperation) return

        val date = diary.date
        launchWithUnexpectedErrorHandler {
            navigatePreviousScreen(date)
        }
    }

    /**
     * 日記読み込み失敗ダイアログから結果を受け取った時に呼び出される事を想定。
     * 前の画面へ遷移する。
     */
    internal fun onDiaryLoadFailureDialogResultReceived() {
        launchWithUnexpectedErrorHandler {
            navigatePreviousScreenOnDiaryLoadFailed()
        }
    }

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
     * 日記削除パラメータ([clearPendingDiaryDeleteParameters])をクリアする。
     */
    internal fun onDiaryDeleteDialogNegativeResultReceived() {
        clearPendingDiaryDeleteParameters()
    }
    //endregion

    //region Business Logic
    /**
     * IDに基づいて日記を読み込む。
     * @param id 読み込む日記のID
     * @param date 読み込む日記の日付（エラー時のダイアログ表示用）
     */
    private suspend fun loadDiary(id: DiaryId, date: LocalDate) {
        val logMsg = "日記読込"
        Log.i(logTag, "${logMsg}_開始")

        updateToDiaryLoadingState()
        when (val result = loadDiaryByIdUseCase(id)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                val diary = result.value.toUiModel()
                updateToDiaryLoadSuccessState(diary)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                when (result.exception) {
                    is DiaryLoadByIdException.LoadFailure -> {
                        updateToDiaryLoadErrorState()
                        emitNavigationEvent(
                            NavigationEvent.To(
                                DiaryShowNavDestination.DiaryLoadFailureDialog(date),
                                NavigationEvent.Policy.Retry
                            )
                        )
                    }
                    is DiaryLoadByIdException.Unknown -> {
                        updateToDiaryLoadErrorState()
                        showUnexpectedAppMessageDialog(result.exception)
                    }
                }
            }
        }
    }

    /**
     * 日記の削除確認ダイアログを表示する。
     * 渡されたパラメータをキャッシュし、ダイアログを表示する（イベント発行）。
     *
     * @param id 削除対象の日記のID。
     * @param date 削除対象の日記の日付。
     */
    private suspend fun showDiaryDeleteDialog(id: DiaryId, date: LocalDate) {
        cachePendingDiaryDeleteParameters(id, date)
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryShowNavDestination.DiaryDeleteDialog(date),
                NavigationEvent.Policy.Single
            )
        )
    }

    /**
     * 日記を削除し、前の画面へ遷移する（イベント発行）。
     * @param id 削除対象の日記ID
     * @param date 削除対象の日記の日付
     */
    private suspend fun deleteDiary(id: DiaryId, date: LocalDate) {
        val logMsg = "日記削除"
        Log.i(logTag, "${logMsg}_開始")

        updateToProgressVisibleState()
        when (val result = deleteDiaryUseCase(id)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}_完了")
                updateToProgressInvisibleState()
                emitNavigationEvent(
                    NavigationEvent.Back(
                        NavigationEvent.Policy.Retry,
                        date
                    )
                )
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}_失敗", result.exception)
                updateToProgressInvisibleState()
                when (result.exception) {
                    is DiaryDeleteException.DiaryDataDeleteFailure -> {
                        showAppMessageDialog(DiaryShowAppMessage.DiaryDeleteFailure)
                    }
                    is DiaryDeleteException.ImageFileDeleteFailure -> {
                        showAppMessageDialog(DiaryShowAppMessage.DiaryImageDeleteFailure)
                    }
                    is DiaryDeleteException.Unknown -> {
                        showUnexpectedAppMessageDialog(result.exception)
                    }
                }
            }
        }
    }

    /**
     * 前の画面へ遷移する（イベント発行）。
     * @param diaryDate 遷移元に返す日記の日付
     */
    private suspend fun navigatePreviousScreen(diaryDate: LocalDate) {
        emitNavigationEvent(
            NavigationEvent.Back(
                NavigationEvent.Policy.Single,
                diaryDate
            )
        )
    }

    /**
     * 前の画面へ遷移する（イベント発行）。
     * 日記読み込み失敗時に呼び出す。
     */
    private suspend fun navigatePreviousScreenOnDiaryLoadFailed() {
        emitNavigationEvent(
            NavigationEvent.Back(
                NavigationEvent.Policy.Retry,
                null
            )
        )
    }

    /**
     * 日記編集画面への遷移する（イベント発行）。
     *
     * @param id 編集対象の日記のID。
     * @param date 編集対象の日記の日付。
     */
    private suspend fun navigateDiaryEditScreen(id: String, date: LocalDate) {
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryShowNavDestination.DiaryEditScreen(id, date),
                NavigationEvent.Policy.Single
            )
        )
    }

    /**
     * アプリケーションメッセージダイアログを表示する（イベント発行）。
     * @param appMessage 表示するメッセージ。
     */
    private suspend fun showAppMessageDialog(appMessage: DiaryShowAppMessage) {
        emitNavigationEvent(
            NavigationEvent.To(
                DiaryShowNavDestination.AppMessageDialog(appMessage),
                NavigationEvent.Policy.Retry
            )
        )
    }

    override suspend fun showUnexpectedAppMessageDialog(e: Exception) {
        showAppMessageDialog(DiaryShowAppMessage.Unexpected(e))
    }
    //endregion

    //region UI State Update
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
                isInputDisabled = true
            )
        }
    }

    /** UIを処理中の状態に更新する。 */
    private fun updateToProgressVisibleState() {
        updateUiState {
            it.copy(
                isProcessing = true,
                isInputDisabled = true
            )
        }
    }

    /** UIを処理完了の状態に更新する。 */
    private fun updateToProgressInvisibleState() {
        updateUiState {
            it.copy(
                isProcessing = false,
                isInputDisabled = false
            )
        }
    }
    //endregion

    //region Pending Diary Delete Parameters
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
     * */
    private data class DiaryDeleteParameters(
        val id: DiaryId,
        val date: LocalDate
    )
    //endregion
}
