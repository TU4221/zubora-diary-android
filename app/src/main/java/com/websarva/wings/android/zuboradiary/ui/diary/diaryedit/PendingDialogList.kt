package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit


class PendingDialogList {

    private val pendingDialogList: List<PendingDialog>

    val isEmpty
        get() = pendingDialogList.isEmpty()

    constructor(pendingDialogList: List<PendingDialog>) {
        this.pendingDialogList = pendingDialogList.toList()
    }

    constructor(): this(ArrayList())

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
