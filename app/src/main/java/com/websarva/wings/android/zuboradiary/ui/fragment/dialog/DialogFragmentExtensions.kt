package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import androidx.fragment.app.DialogFragment
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

/**
 * このDialogFragmentから呼び出し元のFragmentへ、指定されたキーで結果を送信する。
 *
 * この関数は、Navigation Componentの[NavController.navigate]で遷移してきたダイアログから、
 * [NavController.previousBackStackEntry]を利用して遷移元のFragmentへ
 * 結果を返すために使用される。
 *
 * 呼び出し元のFragmentでは、対象の[NavBackStackEntry]の[SavedStateHandle]を監視することで
 * この結果を受け取ることができる。
 *
 * @param T 送信する結果のデータ型。
 * @param resultKey 結果を識別するためのキー文字列。
 * @param result 送信する結果オブジェクト。
 */
internal fun <T> DialogFragment.setResult(resultKey: String, result: DialogResult<T>) {
    val navBackStackEntry = checkNotNull(findNavController().previousBackStackEntry)
    val savedStateHandle = navBackStackEntry.savedStateHandle
    savedStateHandle[resultKey] = result
}
