package com.websarva.wings.android.zuboradiary.ui.diary.common.state

import com.websarva.wings.android.zuboradiary.ui.common.model.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.common.state.LoadState
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryUi

/**
 * 日記表示に関連する画面のUI状態が共通して持つべきプロパティを定義するインターフェース。
 */
interface BaseDiaryShowUiState {

    /** 日記データの読み込み状態。 */
    val diaryLoadState: LoadState<DiaryUi>

    /** 天気2が表示されるべきかを示す。 */
    val isWeather2Visible: Boolean

    /** 表示されている日記項目の数。 */
    val numVisibleDiaryItems: Int

    /** 添付画像のファイルパス。 */
    val diaryImageFilePath: FilePathUi?
}
