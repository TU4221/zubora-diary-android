package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.domain.model.diary.Condition
import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather

internal fun DiaryEntity.toDomainModel(): Diary {
    return Diary(
        DiaryId(id),
        date,
        log,
        Weather.of(weather1),
        Weather.of(weather2),
        Condition.of(condition),
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
        imageFileName?.let { DiaryImageFileName(it) }
    )
}

internal fun Diary.toDataModel(): DiaryEntity {
    return DiaryEntity(
        id.value,
        date,
        log,
        weather1.number,
        weather2.number,
        condition.number,
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
        imageFileName?.fullName
    )
}
