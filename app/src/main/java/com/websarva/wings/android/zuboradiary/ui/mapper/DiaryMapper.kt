package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.ui.model.DiaryUi

internal fun Diary.toUiModel(): DiaryUi {
    return DiaryUi(
        id.toUiModel(),
        date,
        log,
        weather1.toUiModel(),
        weather2.toUiModel(),
        condition.toUiModel(),
        title,
        item1Title,
        item1Comment,
        item2Title,
        item2Comment,
        item3Title,
        item3Comment,
        item4Title,
        item4Comment,
        item5Title,
        item5Comment,
        imageFileName?.toUiModel()
    )
}

internal fun DiaryUi.toDomainModel(): Diary {
    return Diary(
        id.toDomainModel(),
        date,
        log,
        weather1.toDomainModel(),
        weather2.toDomainModel(),
        condition.toDomainModel(),
        title,
        item1Title,
        item1Comment,
        item2Title,
        item2Comment,
        item3Title,
        item3Comment,
        item4Title,
        item4Comment,
        item5Title,
        item5Comment,
        imageFileName?.toDomainModel()
    )
}
