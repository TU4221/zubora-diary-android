package com.websarva.wings.android.zuboradiary.data.mapper

import com.websarva.wings.android.zuboradiary.data.database.DiaryEntity
import com.websarva.wings.android.zuboradiary.domain.model.Condition
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import java.time.LocalDate
import java.time.LocalDateTime

internal fun DiaryEntity.toDomainModel(): Diary {
    return Diary(
        LocalDate.parse(date),
        LocalDateTime.parse(log),
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
        imageUriString
    )
}

internal fun Diary.toDataModel(): DiaryEntity {
    return DiaryEntity(
        date.toString(),
        log.toString(),
        weather1.toNumber(),
        weather2.toNumber(),
        condition.toNumber(),
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
        imageUriString
    )
}
