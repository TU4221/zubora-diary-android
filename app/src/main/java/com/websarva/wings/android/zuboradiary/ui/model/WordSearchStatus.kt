package com.websarva.wings.android.zuboradiary.ui.model

internal sealed class WordSearchStatus {
    data object Idle : WordSearchStatus() // 検索前
    data object Searching : WordSearchStatus() // 検索中
    data object Results : WordSearchStatus() // 検索結果表示
    data object NoResults : WordSearchStatus() // 検索結果なし
    data object Updating : WordSearchStatus() // 検索結果更新中
}
