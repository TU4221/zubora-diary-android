package com.websarva.wings.android.zuboradiary.data

class AppMessageList {

    private val appMessageList: List<AppMessage>

    val isEmpty
        get() = appMessageList.isEmpty()

    constructor(appMessageList: List<AppMessage>) {
        this.appMessageList = appMessageList.toList()
    }

    constructor(): this(ArrayList())

    fun add(appMessage: AppMessage): AppMessageList {
        val resultList = appMessageList + appMessage
        return AppMessageList(resultList)
    }

    fun removeFirstItem(): AppMessageList {
        val resultList = appMessageList.toMutableList()
        if (resultList.isNotEmpty()) resultList.removeAt(0)
        return AppMessageList(resultList)
    }

    fun equalLastItem(appMessage: AppMessage): Boolean {
        if (appMessageList.isEmpty()) return false

        val lastPosition = appMessageList.size - 1
        val lastAppMessage = appMessageList[lastPosition]
        return lastAppMessage == appMessage
    }

    fun findFirstItem(): AppMessage? {
        if (appMessageList.isEmpty()) return null
        return appMessageList[0]
    }
}
