package com.websarva.wings.android.zuboradiary.ui.model.common

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * ファイルパスを表現するためのUIモデル。
 * パスの有効性（利用可能か不可能か）に応じて状態を持つ。
 */
@Parcelize
sealed class FilePathUi : Parcelable {

    /** ファイルシステムの実際のパス文字列。 */
    abstract val path: String

    /**
     * 利用可能なファイルパスを表す。
     * @property path 有効なファイルパス文字列。
     */
    data class Available(override val path: String) : FilePathUi()

    /**
     * 利用不可能な、または存在しないファイルパスを表す。
     * @property path 無効なファイルパス文字列。デフォルト値は空文字列。
     */
    object Unavailable : FilePathUi() {
        @IgnoredOnParcel
        override val path: String = ""
    }
}
