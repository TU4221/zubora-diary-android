package com.websarva.wings.android.zuboradiary.ui.model.parameters

import com.websarva.wings.android.zuboradiary.domain.model.Diary
import java.io.Serializable

// MEMO:日記表示、編集フラグメント以外からも削除できるように下記データ構成とする。
internal data class NavigatePreviousParameters(
    val loadedDiary: Diary?
) : Serializable
