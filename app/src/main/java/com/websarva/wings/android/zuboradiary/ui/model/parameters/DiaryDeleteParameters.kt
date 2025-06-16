package com.websarva.wings.android.zuboradiary.ui.model.parameters

import android.net.Uri
import java.io.Serializable
import java.time.LocalDate

internal data class DiaryDeleteParameters(
    val loadedDate: LocalDate,
    val loadedPicturePath: Uri?
) : Serializable
