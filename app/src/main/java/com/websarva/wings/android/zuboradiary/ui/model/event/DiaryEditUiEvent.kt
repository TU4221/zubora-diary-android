package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.DiaryEditFragment
import com.websarva.wings.android.zuboradiary.ui.model.diary.item.DiaryItemTitleSelectionUi
import java.time.LocalDate

/**
 * 日記編集画面([DiaryEditFragment])における、UIイベントを表すsealed class。
 */
sealed class DiaryEditUiEvent : UiEvent {

    /**
     * 日記表示画面へ遷移することを示すイベント。
     * @property id 表示対象の日記ID。
     * @property date 対象の日記の日付。
     */
    data class NavigateDiaryShowScreen(val id: String, val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 前の画面へデータを渡し、遷移することを示すイベント。
     * @param originalDiaryDate 遷移元に返す編集元日記の日付
     */
    data class NavigatePreviousScreenWithResult(
        val originalDiaryDate: LocalDate
    ) : DiaryEditUiEvent()

    /**
     * 日記削除後に前の画面へ遷移することを示すイベント。
     * @property date 削除された日記の日付。
     */
    data class NavigatePreviousScreenOnDiaryDelete(
        val date: LocalDate
    ) : DiaryEditUiEvent()

    /** 日記の初期読み込み失敗後に前の画面へ遷移することを示すイベント。 */
    data object NavigatePreviousScreenOnInitialDiaryLoadFailed : DiaryEditUiEvent()

    /**
     * 日記項目タイトル編集ダイアログを表示することを示すイベント。
     * @property diaryItemTitleSelection 編集対象のタイトル情報。
     */
    data class ShowDiaryItemTitleEditDialog(
        val diaryItemTitleSelection: DiaryItemTitleSelectionUi
    ) : DiaryEditUiEvent()

    /**
     * 既存日記の読み込み確認ダイアログを表示することを示すイベント。
     * @property date 読み込み対象の日付。
     */
    data class ShowDiaryLoadDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記読み込み失敗ダイアログを表示することを示すイベント。
     * @property date 読み込みに失敗した日記の日付。
     */
    data class ShowDiaryLoadFailureDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記の上書き保存確認ダイアログを表示することを示すイベント。
     * @property date 上書き対象の日付。
     */
    data class ShowDiaryUpdateDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記削除確認ダイアログを表示することを示すイベント。
     * @property date 削除対象の日付。
     */
    data class ShowDiaryDeleteDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日付選択ダイアログを表示することを示すイベント。
     * @property date 初期選択する日付。
     */
    data class ShowDatePickerDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 天気情報取得確認ダイアログを表示することを示すイベント。
     * @property date 天気情報を取得する日付。
     */
    data class ShowWeatherInfoFetchDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記項目削除確認ダイアログを表示することを示すイベント。
     * @property itemNumber 削除対象の項目番号。
     */
    data class ShowDiaryItemDeleteDialog(val itemNumber: Int) : DiaryEditUiEvent()

    /** 添付画像削除確認ダイアログを表示することを示すイベント。 */
    data object ShowDiaryImageDeleteDialog : DiaryEditUiEvent()

    /** 未保存終了確認ダイアログを表示することを示すイベント。 */
    data object ShowExitWithoutDiarySaveDialog : DiaryEditUiEvent()

    /** ギャラリーから画像を選択する処理を開始することを示すイベント。 */
    data object ShowImageSelectionGallery : DiaryEditUiEvent()

    /**
     * 日記項目を追加（表示）するアニメーションを開始することを示すイベント。
     * @property itemNumber 表示にする項目の番号。
     */
    data class startDiaryItemAdditionAnimation(val itemNumber: Int) : DiaryEditUiEvent()

    /**
     * 日記項目を削除（非表示）にするアニメーションを開始することを示すイベント。
     * @property itemNumber 非表示にする項目の番号。
     */
    data class startDiaryItemDeleteAnimation(val itemNumber: Int) : DiaryEditUiEvent()

    /** 天気情報を取得する前に、位置情報権限を確認することを示すイベント。 */
    data object CheckAccessLocationPermissionBeforeWeatherInfoFetch : DiaryEditUiEvent()
}
