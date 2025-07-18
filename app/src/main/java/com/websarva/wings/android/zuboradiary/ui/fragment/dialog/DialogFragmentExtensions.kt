package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

internal fun <T> DialogFragment.setResult(resultKey: String, result: DialogResult<T>) {
    val navBackStackEntry = checkNotNull(findNavController().previousBackStackEntry)
    val savedStateHandle = navBackStackEntry.savedStateHandle
    savedStateHandle[resultKey] = result
}
