package com.websarva.wings.android.zuboradiary.di.data

import android.content.Context
import com.websarva.wings.android.zuboradiary.data.file.ImageFileDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ファイル操作関連の依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object FileModule {

    @Singleton
    @Provides
    fun provideImageFileDataSource(
        @ApplicationContext context: Context
    ):  ImageFileDataSource =
        ImageFileDataSource(
            context.contentResolver,
            context.cacheDir,
            context.filesDir
        )
}
