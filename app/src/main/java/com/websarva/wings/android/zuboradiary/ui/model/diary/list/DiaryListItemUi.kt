package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.YearMonth

@Parcelize
sealed class DiaryListItemUi<T: DiaryListItemContainerUi> : Parcelable {

    data class Header<T: DiaryListItemContainerUi>(
        val yearMonth: YearMonth
    ) : DiaryListItemUi<T>()

    data class Diary<T: DiaryListItemContainerUi>(
        val containerUi: T
    ) : DiaryListItemUi<T>()

    class NoDiaryMessage<T: DiaryListItemContainerUi> : DiaryListItemUi<T>()

    class ProgressIndicator<T: DiaryListItemContainerUi> : DiaryListItemUi<T>()
}
