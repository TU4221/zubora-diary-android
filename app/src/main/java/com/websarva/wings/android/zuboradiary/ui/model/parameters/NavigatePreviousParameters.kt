package com.websarva.wings.android.zuboradiary.ui.model.parameters

import com.websarva.wings.android.zuboradiary.domain.model.Diary
import java.io.Serializable

internal data class NavigatePreviousParameters(
    val originalDiary: Diary?
) : Serializable
