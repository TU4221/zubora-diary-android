package com.websarva.wings.android.zuboradiary.ui.model.state.ui

import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import com.websarva.wings.android.zuboradiary.ui.model.state.LoadState

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
