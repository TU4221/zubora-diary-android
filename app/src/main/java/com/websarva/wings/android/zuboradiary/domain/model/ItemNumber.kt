package com.websarva.wings.android.zuboradiary.domain.model

import java.io.Serializable

@JvmInline
internal value class ItemNumber(val value: Int) : Serializable, Comparable<ItemNumber> {

    companion object {
        const val MIN_NUMBER: Int = 1
        const val MAX_NUMBER: Int = 5
    }

    val isMinNumber get() = value == MIN_NUMBER

    val isMaxNumber get() = value == MAX_NUMBER


    init {
        require(value >= MIN_NUMBER)
        require(value <= MAX_NUMBER)
    }

    fun inc(): ItemNumber {
        val value = this.value.inc()
        require(value <= MAX_NUMBER)

        return ItemNumber(value)
    }

    override fun compareTo(other: ItemNumber): Int {
        return this.value.compareTo(other.value)
    }
}
