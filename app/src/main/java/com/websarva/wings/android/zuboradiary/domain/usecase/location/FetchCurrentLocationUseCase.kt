package com.websarva.wings.android.zuboradiary.domain.usecase.location

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.SimpleLocation
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.LocationException
import com.websarva.wings.android.zuboradiary.domain.usecase.location.exception.CurrentLocationFetchException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 現在位置情報を取得するユースケース。
 *
 * @property locationRepository 位置情報関連の操作を行うリポジトリ。
 */
internal class FetchCurrentLocationUseCase(
    private val locationRepository: LocationRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "現在位置情報取得_"

    /**
     * ユースケースを実行し、現在位置情報を取得する。
     *
     * @param isGranted 位置情報権限が付与されているかどうか。
     * @return 取得した位置情報 ([SimpleLocation]) を [UseCaseResult.Success] に格納して返す。
     *   処理中にエラーが発生した場合は、対応する [CurrentLocationFetchException] を
     *   [UseCaseResult.Failure] に格納して返す。
     */
    suspend operator fun invoke(
        isGranted: Boolean
    ): UseCaseResult<SimpleLocation, CurrentLocationFetchException> {
        Log.i(logTag, "${logMsg}開始 (権限付与: $isGranted)")

        if (!isGranted) {
            val exception = CurrentLocationFetchException.LocationPermissionNotGranted()
            Log.w(logTag, "${logMsg}失敗_位置情報権限未取得", exception)
            return UseCaseResult.Failure(exception)
        }

        return try {
            val location = locationRepository.fetchCurrentLocation()
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(location)
        } catch (e: LocationException) {
            Log.e(logTag, "${logMsg}失敗_位置情報アクセスエラー", e)
            UseCaseResult.Failure(
                CurrentLocationFetchException.LocationAccessFailure(e)
            )
        }
    }
}
