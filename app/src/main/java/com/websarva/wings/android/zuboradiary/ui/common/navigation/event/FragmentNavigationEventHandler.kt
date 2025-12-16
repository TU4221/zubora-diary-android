package com.websarva.wings.android.zuboradiary.ui.common.navigation.event

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections

/**
 * Fragmentにおける画面遷移イベントのハンドリングに必要な定義を提供するインターフェース。
 *
 * 以下の情報を提供する:
 * - 現在地の検証用ID
 * - 結果受け渡し用のキー
 * - 遷移先オブジェクトから [NavDirections] 、 [NavDestination.id] への変換ロジック
 */
interface FragmentNavigationEventHandler<in ND : AppNavDestination, in NBD : AppNavBackDestination> {

    /**
     * 現在のFragmentに対応する [NavDestination.id] 。
     *
     * [FragmentNavigationEventHelper] にて、遷移操作が可能か検証する際に使用する。
     */
    val destinationId: Int

    /**
     * 結果データの受け渡しに使用するキー。
     *
     * 戻り遷移（Back）時に結果を [SavedStateHandle] に格納する際に使用する。
     * 結果の返却が不要な場合は null を許容する。
     */
    val resultKey: String?

    /**
     * 前方遷移先 [destination] を [NavDirections] へ変換する。
     *
     * @param destination 遷移先の目的地データ。
     * @return [NavController] での遷移に使用する [NavDirections]。
     */
    fun toNavDirections(destination: ND): NavDirections

    /**
     * 後方遷移先 [destination] を [NavDestination.id] へ変換する。
     *
     * @param destination 戻り先の目的地データ。
     * @return [NavController.popBackStack] で使用するリソースID。
     */
    fun toNavDestinationId(destination: NBD): Int
}
