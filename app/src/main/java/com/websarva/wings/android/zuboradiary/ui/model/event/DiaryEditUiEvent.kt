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
    data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記項目タイトル編集ダイアログへ遷移することを示すイベント。
     * @property diaryItemTitleSelection 編集対象のタイトル情報。
     */
    data class NavigateDiaryItemTitleEditDialog(
        val diaryItemTitleSelection: DiaryItemTitleSelectionUi
    ) : DiaryEditUiEvent()

    /**
     * 既存日記の読み込み確認ダイアログへ遷移することを示すイベント。
     * @property date 読み込み対象の日付。
     */
    data class NavigateDiaryLoadDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記読み込み失敗ダイアログへ遷移することを示すイベント。
     * @property date 読み込みに失敗した日記の日付。
     */
    data class NavigateDiaryLoadFailureDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記の上書き保存確認ダイアログへ遷移することを示すイベント。
     * @property date 上書き対象の日付。
     */
    data class NavigateDiaryUpdateDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記削除確認ダイアログへ遷移することを示すイベント。
     * @property date 削除対象の日付。
     */
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日付選択ダイアログへ遷移することを示すイベント。
     * @property date 初期選択する日付。
     */
    data class NavigateDatePickerDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 天気情報取得確認ダイアログへ遷移することを示すイベント。
     * @property date 天気情報を取得する日付。
     */
    data class NavigateWeatherInfoFetchDialog(val date: LocalDate) : DiaryEditUiEvent()

    /**
     * 日記項目削除確認ダイアログへ遷移することを示すイベント。
     * @property itemNumber 削除対象の項目番号。
     */
    data class NavigateDiaryItemDeleteDialog(val itemNumber: Int) : DiaryEditUiEvent()

    /** 添付画像削除確認ダイアログへ遷移することを示すイベント。 */
    data object NavigateDiaryImageDeleteDialog : DiaryEditUiEvent()

    /** 未保存終了確認ダイアログへ遷移することを示すイベント。 */
    data object NavigateExitWithoutDiarySaveDialog : DiaryEditUiEvent()

    /**
     * 前の画面へデータを渡し、遷移することを示すイベント。
     * @param originalDiaryDate 遷移元に返す編集元日記の日付
     */
    data class NavigatePreviousFragmentWithResult(
        val originalDiaryDate: LocalDate
    ) : DiaryEditUiEvent()

    /**
     * 日記削除後に前の画面へ遷移することを示すイベント。
     * @property date 削除された日記の日付。
     */
    data class NavigatePreviousFragmentOnDiaryDelete(
        val date: LocalDate
    ) : DiaryEditUiEvent()

    /** 日記の初期読み込み失敗後に前の画面へ遷移することを示すイベント。 */
    data object NavigatePreviousFragmentOnInitialDiaryLoadFailed : DiaryEditUiEvent()

    /**
     * 日記項目のレイアウトを更新することを示すイベント。
     * @property numVisibleItems 表示する項目の数。
     */
    data class UpdateDiaryItemLayout(val numVisibleItems: Int) : DiaryEditUiEvent()

    /**
     * 日記項目を非表示（削除）にするアニメーションを開始することを示すイベント。
     * @property itemNumber 非表示にする項目の番号。
     */
    data class TransitionDiaryItemToInvisible(val itemNumber: Int) : DiaryEditUiEvent()

    /** 天気情報を取得する前に、位置情報権限を確認することを示すイベント。 */
    data object CheckAccessLocationPermissionBeforeWeatherInfoFetch : DiaryEditUiEvent()

    /** 日記項目を表示（追加）するアニメーション処理の準備を示すイベント。 */
    data object PrepareDiaryItemVisibleTransition : DiaryEditUiEvent()

    /** ギャラリーから画像を選択する処理を開始することを示すイベント。 */
    data object SelectImage : DiaryEditUiEvent()
}
