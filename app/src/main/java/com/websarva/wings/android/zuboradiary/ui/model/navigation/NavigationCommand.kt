package com.websarva.wings.android.zuboradiary.ui.model.navigation

import androidx.navigation.NavDirections
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult

sealed class NavigationCommand {
    data class To(val directions: NavDirections): NavigationCommand()
    data class Up<T>(val resultKey: String? = null, val result: FragmentResult<T> = FragmentResult.None): NavigationCommand()
    data class Pop<T>(val resultKey: String? = null, val result: FragmentResult<T> = FragmentResult.None): NavigationCommand()
    data class PopTo<T>(val destinationId: Int, val inclusive: Boolean, val resultKey: String? = null, val result: FragmentResult<T> = FragmentResult.None): NavigationCommand()
}
