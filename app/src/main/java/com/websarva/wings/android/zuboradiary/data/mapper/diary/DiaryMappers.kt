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
    return Diary.create(
        DiaryId(id),
        date,
        log,
        Weather.of(weather1),
        Weather.of(weather2),
        Condition.of(condition),
        DiaryTitle(title),
        mapOf(
            1 to DiaryItemTitle(item1Title),
            2 to item2Title?.let { DiaryItemTitle(it) },
            3 to item3Title?.let { DiaryItemTitle(it) },
            4 to item4Title?.let { DiaryItemTitle(it) },
            5 to item5Title?.let { DiaryItemTitle(it) }
        ),
        mapOf(
            1 to DiaryItemComment(item1Comment),
            2 to item2Comment?.let { DiaryItemComment(it) },
            3 to item3Comment?.let { DiaryItemComment(it) },
            4 to item4Comment?.let { DiaryItemComment(it) },
            5 to item5Comment?.let { DiaryItemComment(it) }
        ),
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
        itemTitles[1]!!.value,
        itemComments[1]!!.value,
        itemTitles[2]?.value,
        itemComments[2]?.value,
        itemTitles[3]?.value,
        itemComments[3]?.value,
        itemTitles[4]?.value,
        itemComments[4]?.value,
        itemTitles[5]?.value,
        itemComments[5]?.value,
        imageFileName?.fullName
    )
}
