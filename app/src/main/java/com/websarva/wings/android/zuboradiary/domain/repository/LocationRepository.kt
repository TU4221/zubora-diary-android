package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.model.SimpleLocation
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.exception.LocationException
import com.websarva.wings.android.zuboradiary.domain.exception.PermissionException

/**
 * 位置情報の取得処理を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、特定の日付の位置情報を取得する機能を提供します。
 *
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外 ([UseCaseException] のサブクラス) をスローする可能性があります。
 */
internal interface LocationRepository {

    /**
     * 位置情報を取得する。
     *
     * @return 取得された位置情報。
     * @throws PermissionException 位置情報へのアクセス権限がない場合。
     * @throws LocationException 位置情報の取得に失敗した場合。
     */
    suspend fun fetchCurrentLocation(): SimpleLocation
}
