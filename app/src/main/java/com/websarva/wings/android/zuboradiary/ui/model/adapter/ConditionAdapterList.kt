package com.websarva.wings.android.zuboradiary.ui.model.adapter

import android.content.Context
import com.websarva.wings.android.zuboradiary.data.model.Condition

internal class ConditionAdapterList {

    private val adapterList = Condition.entries.toList()

    fun toStringList(context: Context): List<String> {
        val stringList = ArrayList<String>()
        adapterList.forEach { value: Condition ->
            stringList.add(value.toString(context))
        }
        return stringList
    }
}
