package com.websarva.wings.android.zuboradiary.ui.model.parameters

import java.io.Serializable
import java.time.LocalDate

internal data class DiaryLoadingParameters(
    val date: LocalDate
) : Serializable
