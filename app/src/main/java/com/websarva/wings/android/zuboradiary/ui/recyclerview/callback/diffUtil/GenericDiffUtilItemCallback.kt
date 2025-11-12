package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.diffUtil

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable

internal class GenericDiffUtilItemCallback<T: Any> : DiffUtil.ItemCallback<T>() {

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

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        val result = oldItem == newItem
        Log.d(
            logTag,
            "areContentsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
        )
        return result
    }
}
