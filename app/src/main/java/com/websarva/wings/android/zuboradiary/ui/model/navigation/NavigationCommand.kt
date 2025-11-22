package com.websarva.wings.android.zuboradiary.ui.model.navigation

import androidx.navigation.NavDirections
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult

/**
 * ナビゲーションを操作するためのコマンドを表すsealed class。
 *
 * Navigation Componentの各操作（特定の画面への遷移、バックスタックを遡る、特定の画面までポップするなど）をカプセル化する。
 */
sealed class NavigationCommand {
    /**
     * [NavDirections]を使用して、特定の画面へ遷移することを示す。
     * @property directions 遷移先と引数を定義したオブジェクト。
     */
    data class To(val directions: NavDirections): NavigationCommand()

    /**
     * ナビゲーションのバックスタックを一つ遡る（前の画面に戻る）ことを示す。
     * @param result 遷移元の画面に返すデータ。
     */
    data class Up<T>(val result: FragmentResult<T>): NavigationCommand()

    /**
     * 現在の画面をバックスタックからポップすることを示す。
     * @param result 遷移元の画面に返すデータ。
     */
    data class Pop<T>(val result: FragmentResult<T>): NavigationCommand()

    /**
     * 指定されたIDの画面まで、バックスタックをポップすることを示す。
     * @param destinationId ポップする先の画面のID。
     * @param inclusive `true`の場合、[destinationId]で指定された画面もバックスタックからポップする。
     * @param result 遷移元の画面に返すデータ。
     */
    data class PopTo<T>(
        val destinationId: Int,
        val inclusive: Boolean,
        val result: FragmentResult<T>
    ): NavigationCommand()
}
