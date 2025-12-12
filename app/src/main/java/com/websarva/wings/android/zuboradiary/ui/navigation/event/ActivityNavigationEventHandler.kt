package com.websarva.wings.android.zuboradiary.ui.navigation.event

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.AppNavDestination

/**
 * Activityにおける画面遷移イベントのハンドリングに必要な定義を提供するインターフェース。
 *
 * 以下の情報を提供する:
 * - 遷移先オブジェクトから [NavDirections] への変換ロジック
 */
interface ActivityNavigationEventHandler<in ND : AppNavDestination> {

    /**
     * 前方遷移先 [destination] を [NavDirections] へ変換する。
     *
     * @param destination 遷移先の目的地データ。
     * @return [NavController] での遷移に使用する [NavDirections]。
     */
    fun toNavDirections(destination: ND): NavDirections
}
