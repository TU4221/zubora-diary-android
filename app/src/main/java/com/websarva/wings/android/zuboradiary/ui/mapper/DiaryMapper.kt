package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.ui.model.DiaryUi

internal fun Diary.toUiModel(): DiaryUi {
    return DiaryUi(
        id.value,
        date,
        log,
        weather1.toUiModel(),
        weather2.toUiModel(),
        condition.toUiModel(),
        title.value,
        item1Title.value,
        item1Comment.value,
        item2Title?.value,
        item2Comment?.value,
        item3Title?.value,
        item3Comment?.value,
        item4Title?.value,
        item4Comment?.value,
        item5Title?.value,
        item5Comment?.value,
        imageFileName?.toUiModel()
    )
}

internal fun DiaryUi.toDomainModel(): Diary {
    return Diary(
        DiaryId(id),
        date,
        log,
        weather1.toDomainModel(),
        weather2.toDomainModel(),
        condition.toDomainModel(),
        DiaryTitle(title),
        DiaryItemTitle(item1Title),
        DiaryItemComment(item1Comment),
        item2Title?.let { DiaryItemTitle(it) },
        item2Comment?.let { DiaryItemComment(it) },
        item3Title?.let { DiaryItemTitle(it) },
        item3Comment?.let { DiaryItemComment(it) },
        item4Title?.let { DiaryItemTitle(it) },
        item4Comment?.let { DiaryItemComment(it) },
        item5Title?.let { DiaryItemTitle(it) },
        item5Comment?.let { DiaryItemComment(it) },
        imageFileName?.toDomainModel()
    )
}
