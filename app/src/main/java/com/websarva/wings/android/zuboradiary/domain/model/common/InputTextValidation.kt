package com.websarva.wings.android.zuboradiary.domain.model.common

/**
 * 入力テキストの検証状態を表すsealed class。
 *
 * このクラスは、テキスト入力の検証結果としてありうる状態を網羅的に表現する。
 */
internal sealed class InputTextValidation {

    /**
     * テキストが検証ルールを満たし、有効であることを示す。
     */
    data object Valid : InputTextValidation()

    /**
     * テキストが未入力（空文字）であることを示す。
     */
    data object Empty : InputTextValidation()

    /**
     * テキストの先頭文字が、特定の検証ルールに一致しないことを示す。
     */
    data object InitialCharUnmatched : InputTextValidation()
}
