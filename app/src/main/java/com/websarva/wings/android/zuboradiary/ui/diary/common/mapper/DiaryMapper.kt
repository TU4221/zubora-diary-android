package com.websarva.wings.android.zuboradiary.ui.diary.common.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.ui.diary.common.model.DiaryUi

internal fun Diary.toUiModel(): DiaryUi {
    return DiaryUi(
        id.value,
        date,
        log,
        weather1.toUiModel(),
        weather2.toUiModel(),
        condition.toUiModel(),
        title.value,
        itemTitles.mapValues { it.value?.value },
        itemComments.mapValues { it.value?.value },
        imageFileName?.fullName
    )
}

internal fun DiaryUi.toDomainModel(): Diary {
    return Diary.create(
        DiaryId(id),
        date,
        log,
        weather1.toDomainModel(),
        weather2.toDomainModel(),
        condition.toDomainModel(),
        DiaryTitle(title.trim()),
        itemTitles.mapValues { it.value?.let { value -> DiaryItemTitle(value.trim()) } },
        itemComments.mapValues { it.value?.let { value -> DiaryItemComment(value.trim()) } },
        imageFileName?.let { DiaryImageFileName(it) }
    )
}
