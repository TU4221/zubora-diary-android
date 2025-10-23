package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.diary.Diary
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemComment
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryTitle
import com.websarva.wings.android.zuboradiary.ui.model.diary.DiaryUi
import kotlin.text.trim

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
        mapOf(
            1 to item1Title.value,
            2 to item2Title?.value,
            3 to item3Title?.value,
            4 to item4Title?.value,
            5 to item5Title?.value
        ),
        mapOf(
            1 to item1Comment.value,
            2 to item2Comment?.value,
            3 to item3Comment?.value,
            4 to item4Comment?.value,
            5 to item5Comment?.value
        ),
        imageFileName?.fullName
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
        DiaryTitle(title.trim()),
        DiaryItemTitle(item1Title.trim()),
        DiaryItemComment(item1Comment.trim()),
        item2Title?.let { DiaryItemTitle(it.trim()) },
        item2Comment?.let { DiaryItemComment(it.trim()) },
        item3Title?.let { DiaryItemTitle(it.trim()) },
        item3Comment?.let { DiaryItemComment(it.trim()) },
        item4Title?.let { DiaryItemTitle(it.trim()) },
        item4Comment?.let { DiaryItemComment(it.trim()) },
        item5Title?.let { DiaryItemTitle(it.trim()) },
        item5Comment?.let { DiaryItemComment(it.trim()) },
        imageFileName?.let { DiaryImageFileName(it) }
    )
}
