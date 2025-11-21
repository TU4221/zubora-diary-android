package com.websarva.wings.android.zuboradiary.ui.model.diary.list

import android.os.Parcelable
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.common.Identifiable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * 日記一覧画面に表示するアイテムのコンテナを表すUIモデル。
 *
 * このsealed classは、通常の日記リスト項目([Standard])と、
 * ワード検索結果のリスト項目([WordSearchResult])の2種類を表現する。
 *
 * @property id アイテムの一意な識別子。
 * @property date アイテムの日付。
 */
@Parcelize
sealed class DiaryListItemContainerUi(
    override val id: String,
    open val date: LocalDate
) : Parcelable, Identifiable {

    /**
     * 通常の日記リスト画面で表示される標準的なリスト項目。
     *
     * @property id 日記の識別番号。
     * @property date 日記の日付。
     * @property title 日記のタイトル。
     * @property imageFileName 日記に添付した画像ファイル名。未添付の場合 `null`。
     * @property imageFilePath 添付画像の表示用ファイルパス。
     */
    data class Standard(
        override val id: String,
        override val date: LocalDate,
        val title: String,
        val imageFileName: String?,
        val imageFilePath: FilePathUi?
    ) : DiaryListItemContainerUi(id, date)

    /**
     * ワード検索結果画面で表示されるリスト項目。
     * 検索キーワードに一致した項目情報も含まれる。
     *
     * 検索キーワードが日記のタイトルのみに一致した場合は、
     * 項目1の内容が[itemNumber]、[itemTitle]、[itemComment]に格納される。
     *
     * @property id 日記の識別番号。
     * @property date 日記の日付。
     * @property title 日記のタイトル。
     * @property itemNumber 検索ワードに一致した項目の番号。
     * @property itemTitle 検索ワードに一致した項目のタイトル。
     * @property itemComment 検索ワードに一致した項目のコメント。
     * @property searchWord 検索された単語。
     */
    data class WordSearchResult(
        override val id: String,
        override val date: LocalDate,
        val title: String,
        val itemNumber: Int,
        val itemTitle: String,
        val itemComment: String,
        val searchWord: String,
    ) : DiaryListItemContainerUi(id, date)
}
