package com.websarva.wings.android.zuboradiary.ui


class PendingDialogList {

    private val pendingDialogList: List<PendingDialog>

    constructor(pendingDialogList: List<PendingDialog>) {
        this.pendingDialogList = pendingDialogList.toList()
    }

    constructor(): this(ArrayList())

    constructor(pendingDialogList: PendingDialogList):this(pendingDialogList.pendingDialogList.toList())

    fun add(pendingDialog: PendingDialog): PendingDialogList {
        val resultList = pendingDialogList + pendingDialog
        return PendingDialogList(resultList)
    }

    fun removeFirstItem(): PendingDialogList {
        val resultList = pendingDialogList.toMutableList()
        if (resultList.isNotEmpty()) resultList.removeAt(0)
        return PendingDialogList(resultList)
    }

    fun findFirstItem(): PendingDialog? {
        return try {
            pendingDialogList.first()
        } catch (e: NoSuchElementException) {
            null
        }
    }
}
