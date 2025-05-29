package com.websarva.wings.android.zuboradiary.ui.model.result

import android.net.Uri
import java.time.LocalDate

internal data class DiaryListItemDeleteResult(
    val date: LocalDate,
    val uri: Uri?
)
