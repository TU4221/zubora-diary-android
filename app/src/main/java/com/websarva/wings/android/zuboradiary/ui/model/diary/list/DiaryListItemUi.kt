package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.YearMonth

/**
 * 日記一覧画面のRecyclerViewに表示するアイテムを表すUIモデル。
 *
 * ヘッダー、日記、メッセージ、プログレスインジケーターなど、
 * リストに表示される可能性のある全ての要素を表現する。
 *
 * @param T [DiaryListItemContainerUi]を継承する、日記リストアイテムの具体的なデータコンテナの型。
 */
@Parcelize
sealed interface DiaryListItemUi<T: DiaryListItemContainerUi> : Parcelable {

    /**
     * 年月を表すヘッダーアイテム。
     * @property yearMonth 表示する年月。
     */
    data class Header<T: DiaryListItemContainerUi>(
        val yearMonth: YearMonth
    ) : DiaryListItemUi<T>

    /**
     * 日記のコンテンツを持つアイテム。
     * @property containerUi 日記データのコンテナ。通常の日記か検索結果か等で型が異なる。
     */
    data class Diary<T: DiaryListItemContainerUi>(
        val containerUi: T
    ) : DiaryListItemUi<T>

    /** 日記が一件も存在しない場合に表示するメッセージアイテム。 */
    class NoDiaryMessage<T: DiaryListItemContainerUi> : DiaryListItemUi<T>

    /** 追加読み込み中に表示するプログレスインジケーターアイテム。 */
    class ProgressIndicator<T: DiaryListItemContainerUi> : DiaryListItemUi<T>
}
