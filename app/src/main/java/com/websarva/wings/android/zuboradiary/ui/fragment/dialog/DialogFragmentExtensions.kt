package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

internal fun <T> DialogFragment.setResult(resultKey: String, result: DialogResult<T>) {
    val navController = NavHostFragment.findNavController(this)
    val navBackStackEntry = checkNotNull(navController.previousBackStackEntry)
    val savedStateHandle = navBackStackEntry.savedStateHandle
    savedStateHandle[resultKey] = result
}
