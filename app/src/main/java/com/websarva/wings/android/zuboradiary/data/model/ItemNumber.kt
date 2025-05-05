package com.websarva.wings.android.zuboradiary.data.model

import java.io.Serializable

internal class ItemNumber(value: Int) : Serializable, Comparable<ItemNumber> {

    companion object {
        const val MIN_NUMBER: Int = 1
        const val MAX_NUMBER: Int = 5
    }

    val value: Int

    init {
        require(value >= MIN_NUMBER)
        require(value <= MAX_NUMBER)

        this.value = value
    }

    fun inc(): ItemNumber {
        val value = this.value.inc()
        require(value <= MAX_NUMBER)

        return ItemNumber(value)
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ItemNumber) return false
        return this.value == other.value
    }

    override fun compareTo(other: ItemNumber): Int {
        return this.value.compareTo(other.value)
    }

    override fun hashCode(): Int {
        return value
    }
}
