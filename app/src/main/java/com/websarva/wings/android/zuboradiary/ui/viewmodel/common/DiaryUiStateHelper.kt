package com.websarva.wings.android.zuboradiary.ui.viewmodel.common

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.ui.model.common.FilePathUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import javax.inject.Inject

/**
 * 日記関連のUI Stateから、派生的なUI Stateを生成するヘルパークラス。
 *
 * 以下の責務を持つ:
 * - 天気2の表示状態の決定
 * - 表示される日記項目数の計算
 * - 画像ファイルパスの構築
 */
internal class DiaryUiStateHelper @Inject constructor(
    private val buildDiaryImageFilePathUseCase: BuildDiaryImageFilePathUseCase
) {

    /**
     * 天気2の表示/非表示状態を決定する。
     * @param weather1 天気1
     * @param weather2 天気2
     * @return 天気2を表示する場合はtrue
     */
    fun isWeather2Visible(weather1: WeatherUi, weather2: WeatherUi): Boolean {
        return weather1 != WeatherUi.UNKNOWN && weather2 != WeatherUi.UNKNOWN
    }

    /**
     * 表示されるべき日記項目の数を計算する。
     * nullでない最後の項目のインデックスに基づいて表示項目数を算出する。
     * @param itemTitles 日記の項目タイトルを格納したMap
     * @return 表示する項目の数
     */
    fun calculateNumVisibleDiaryItems(itemTitles: Map<Int, String?>): Int {
        return itemTitles
            .toSortedMap().values.toList()
            .indexOfLast { it != null }.let { if (it == -1) 0 else it + 1 }
    }

    /**
     * 画像ファイル名から、UIで利用可能な[FilePathUi]を構築する。
     * @param imageFileName 対象の画像ファイル名
     * @return 構築された[FilePathUi]。ファイル名がnullまたはパス構築に失敗した場合はnull。
     */
    suspend fun buildImageFilePath(imageFileName: String?): FilePathUi? {
        val fileName = imageFileName ?: return null
        val result =
            buildDiaryImageFilePathUseCase(
                DiaryImageFileName(fileName)
            )
        return when (result) {
            is UseCaseResult.Success -> FilePathUi.Available(result.value)
            is UseCaseResult.Failure -> FilePathUi.Unavailable
        }
    }
}
