package com.websarva.wings.android.zuboradiary.ui.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * データの読み込み状態を表現するための、汎用的なシールドクラス。
 *
 * @param T 読み込むデータの型。必ず [android.os.Parcelable] である必要がある。
 */
@Parcelize
sealed class LoadState<out T : Parcelable> : Parcelable {
    /**
     * 初期状態、またはまだ読み込みが開始されていない状態。
     */
    data object Idle : LoadState<Nothing>()

    /**
     * データを読み込み中の状態。
     */
    data object Loading : LoadState<Nothing>()

    /**
     * データの読み込みが成功した状態。
     * @property data 読み込まれたデータ。
     */
    data class Success<out T : Parcelable>(val data: T) : LoadState<T>()

    /**
     * データが存在しなかった状態。
     */
    data object Empty : LoadState<Nothing>()

    /**
     * データの読み込みが失敗した状態。
     */
    data object Error : LoadState<Nothing>()
}
