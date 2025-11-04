package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult

interface CommonUiEventHandler {
    fun onNavigatePreviousFragmentEventReceived(result: FragmentResult<*>)
    fun onNavigateAppMessageEventReceived(appMessage: AppMessage)
}
