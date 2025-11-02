package com.websarva.wings.android.zuboradiary.ui.fragment.common

import com.websarva.wings.android.zuboradiary.ui.model.message.AppMessage
import com.websarva.wings.android.zuboradiary.ui.model.result.FragmentResult

// TODO:Ui層のアクセス修飾子をpublicに変更してからBaseFragmentに実装
internal interface CommonUiEventHandler {
    fun onNavigatePreviousFragmentEventReceived(result: FragmentResult<*>)
    fun onNavigateAppMessageEventReceived(appMessage: AppMessage)
}
