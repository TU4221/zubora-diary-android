package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.domain.model.Condition
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.FileName
import com.websarva.wings.android.zuboradiary.domain.model.UUIDString
import com.websarva.wings.android.zuboradiary.domain.model.Weather

internal fun DiaryEntity.toDomainModel(): Diary {
    return Diary(
        UUIDString(id),
        date,
        log,
        Weather.of(weather1),
        Weather.of(weather2),
        Condition.of(condition),
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
        imageFileName?.let { FileName(it) }
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
        imageFileName?.fullName
    )
}
