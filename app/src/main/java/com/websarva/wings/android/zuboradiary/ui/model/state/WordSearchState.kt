package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed class WordSearchState : UiState, Parcelable {
    data object Idle: WordSearchState() // 初期状態

    data object Searching : WordSearchState() // 検索中
    data object AdditionLoading : WordSearchState() // 検索結果追加読込
    data object Updating : WordSearchState() // 検索結果更新中

    data object ShowingResultList : WordSearchState() // 検索結果表示
    data object NoResults : WordSearchState() // 検索結果なし
}
