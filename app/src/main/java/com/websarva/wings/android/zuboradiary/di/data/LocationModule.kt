package com.websarva.wings.android.zuboradiary.di.data

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.websarva.wings.android.zuboradiary.data.common.PermissionChecker
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 位置情報関連の依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * FusedLocation関連の生成を担当する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object LocationModule {

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Singleton
    @Provides
    fun provideFusedLocationDataSource(
        fusedLocationProviderClient: FusedLocationProviderClient,
        permissionChecker: PermissionChecker
    ):  FusedLocationDataSource = FusedLocationDataSource(fusedLocationProviderClient, permissionChecker)
}
