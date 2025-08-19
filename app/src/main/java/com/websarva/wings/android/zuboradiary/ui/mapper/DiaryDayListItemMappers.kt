package com.websarva.wings.android.zuboradiary.ui.mapper

import android.net.Uri
import com.websarva.wings.android.zuboradiary.domain.model.DiaryListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem

internal fun DiaryListItem.toUiModel(): DiaryDayListItem.Standard {
    return DiaryDayListItem.Standard(
        date,
        title,
        imageUriString?.let { Uri.parse(it) }
    )
}
