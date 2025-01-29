package com.websarva.wings.android.zuboradiary.ui

import androidx.lifecycle.LiveData

// TODO:DiaryViewModelでしか使用されないようならdiaryディレクトリに本ファイル移動
fun LiveData<String?>.orEmptyString(): String {
    return value ?: ""
}

fun <T> LiveData<T>.checkNotNull(): T {
    return checkNotNull(value)
}
