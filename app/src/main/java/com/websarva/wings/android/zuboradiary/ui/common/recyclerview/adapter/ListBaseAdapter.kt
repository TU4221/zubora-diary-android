package com.websarva.wings.android.zuboradiary.ui.common.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.common.theme.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.common.theme.withTheme

/**
 * アプリケーションのRecyclerViewで共通して利用する、テーマカラー対応のListAdapterの基底クラス。
 *
 * 以下の責務を持つ:
 * - [ListAdapter]を継承し、[DiffUtil]による効率的なリスト更新を提供する。
 * - コンストラクタで受け取った[ThemeColorUi]に基づき、テーマを適用した[LayoutInflater]を生成し、
 *   その[LayoutInflater]を元にViewHolderを生成するようにする。
 * - ViewHolderの生成([createViewHolder])とデータバインド([bindViewHolder])の具体的な実装はサブクラスに委譲する。
 *
 * @param T リストに表示されるアイテムの型。
 * @param VH RecyclerView.ViewHolderを継承するViewHolderの型。
 * @param themeColor アイテムのViewに適用するテーマカラー。
 * @param diffUtilItemCallback リスト内のアイテム間の差分を計算するためのコールバック。
 */
internal abstract class ListBaseAdapter<T, VH : RecyclerView.ViewHolder> protected constructor(
    protected val themeColor: ThemeColorUi,
    diffUtilItemCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffUtilItemCallback) {

    /** テーマを適用したLayoutInflaterを生成し、生成したインフレータをもとに`ViewHolder`を生成する。 */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val themeColorInflater = inflater.withTheme(themeColor)
        return createViewHolder(parent, themeColorInflater, viewType)
    }

    /**
     * ViewHolderを生成する。[onCreateViewHolder]から呼び出される。
     * @param parent 親のViewGroup。
     * @param themeColorInflater テーマが適用されたLayoutInflater。
     * @param viewType ビュータイプ。
     * @return 生成されたViewHolderインスタンス。
     */
    protected abstract fun createViewHolder(
        parent: ViewGroup, themeColorInflater: LayoutInflater, viewType: Int
    ): VH

    /** ViewHolderにデータをバインドする。 */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        bindViewHolder(holder, item)
    }

    /**
     * 指定されたViewHolderに、対応するアイテムのデータをバインドする。[onBindViewHolder]から呼び出される。
     * @param holder データをバインドするViewHolder。
     * @param item バインドするデータアイテム。
     */
    abstract fun bindViewHolder(holder: VH, item: T)

    /**
     * 指定されたポジションのアイテムを取得する。
     * @param position 取得したいアイテムのリスト内での位置。
     * @return 指定された位置のアイテム。
     */
    fun getItemAt(position: Int): T {
        return getItem(position)
    }
}
