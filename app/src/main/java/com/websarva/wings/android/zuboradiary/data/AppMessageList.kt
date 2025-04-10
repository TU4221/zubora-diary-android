package com.websarva.wings.android.zuboradiary.data

class AppMessageList {

    private val appMessageList: List<AppMessage>

    val isEmpty
        get() = appMessageList.isEmpty()

    constructor(appMessageList: List<AppMessage>) {
        this.appMessageList = appMessageList.toList()
    }

    constructor(): this(ArrayList())

    constructor(appMessageList: AppMessageList):this(appMessageList.appMessageList.toList())

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
        try {
            val lastAppMessage = appMessageList.last()
            return lastAppMessage == appMessage
        } catch (e: NoSuchElementException) {
            return false
        }
    }

    fun findFirstItem(): AppMessage? {
        return try {
            appMessageList.first()
        } catch (e: NoSuchElementException) {
            null
        }

    }
}
