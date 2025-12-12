package com.websarva.wings.android.zuboradiary.ui.navigation.event

import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.AppNavBackDestination
import com.websarva.wings.android.zuboradiary.ui.navigation.event.destination.AppNavDestination
import java.util.UUID

/**
 * 画面遷移イベントを表す基底クラス。
 *
 * イベントの一意性を保証する [id] と、処理方法を規定する [policy] を保持する。
 *
 * @param ND 前方遷移先（[AppNavDestination]）の型。
 * @param NBD 後方遷移先（[AppNavBackDestination]）の型。
 * @property id イベント識別用ID。
 * @property policy イベント実行時のポリシー。
 */
sealed class NavigationEvent<out ND: AppNavDestination, out NBD: AppNavBackDestination>(
    override val id: UUID,
    open val policy: Policy
) : Identifiable {

    /**
     * 特定の目的地への前方遷移イベント。
     *
     * @property destination 遷移先の目的地。
     * @property policy 実行ポリシー。
     * @property id イベントID。デフォルトはランダム生成。
     */
    data class To<ND: AppNavDestination>(
        val destination: ND,
        override val policy: Policy,
        override val id: UUID = UUID.randomUUID()
    ): NavigationEvent<ND, Nothing>(id, policy)

    /**
     * 前画面、または指定した目的地への戻り遷移イベント。
     *
     * @param T 結果データの型。
     * @property policy 実行ポリシー。
     * @property resultData 前画面へ渡す結果データ。不要な場合はnull。
     * @property destination 戻り先の目的地。nullの場合は直前の画面へ戻る。
     * @property inclusive 指定した [destination] をバックスタックから削除するかどうか。
     * @property id イベントID。デフォルトはランダム生成。
     */
    data class Back<NBD: AppNavBackDestination, T>(
        override val policy: Policy,
        val resultData: T? = null,
        val destination: NBD? = null,
        val inclusive: Boolean = false,
        override val id: UUID = UUID.randomUUID()
    ): NavigationEvent<Nothing, NBD>(id, policy)

    /**
     * イベントの実行ポリシー定義。
     */
    sealed interface Policy {

        /**
         * 単発実行ポリシー。
         * 重複イベントを無視、または一度だけ処理する場合に使用する。
         */
        data object Single : Policy

        /**
         * 再試行ポリシー。
         * 複数回の実行や、失敗時のリトライを許容する場合に使用する。
         */
        data object Retry : Policy
    }
}
