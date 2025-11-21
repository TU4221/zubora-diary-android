package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.diffUtil

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable

/**
 * [ListAdapter]で利用するための、汎用的な[DiffUtil.ItemCallback]の実装。
 *
 * アイテムが[Identifiable]インターフェースを実装している場合、[areItemsTheSame]ではIDを比較する。
 * そうでない場合は、参照の等価性（`===`）で比較する。
 * [areContentsTheSame]では、常に内容の等価性（`==`）で比較する。
 *
 * @param T リスト内のアイテムの型。
 */
internal class GenericDiffUtilItemCallback<T: Any> : DiffUtil.ItemCallback<T>() {

    /** 2つのアイテムが同じオブジェクトを表しているか（通常はIDで比較）を判定する。 */
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        val result = if (oldItem is Identifiable && newItem is Identifiable) {
            oldItem.id == newItem.id
        } else {
            oldItem === newItem
        }
        Log.d(
            logTag,
            "areItemsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
        )
        return result
    }

    /** 2つのアイテムのデータ内容が同じであるかを判定する。 */
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        val result = oldItem == newItem
        Log.d(
            logTag,
            "areContentsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
        )
        return result
    }
}
