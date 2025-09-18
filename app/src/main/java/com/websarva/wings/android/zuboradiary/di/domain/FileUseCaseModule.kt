package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.file.ClearCacheFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.file.BuildImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.file.MoveFileToPermanentUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.file.CacheDiaryImageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ファイル操作得関連のユースケースの依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object FileUseCaseModule {

    @Singleton
    @Provides
    fun provideBuildImageFilePathUseCase(
        fileRepository: FileRepository
    ): BuildImageFilePathUseCase = BuildImageFilePathUseCase(fileRepository)

    @Singleton
    @Provides
    fun provideClearCacheFileUseCase(
        fileRepository: FileRepository
    ): ClearCacheFileUseCase = ClearCacheFileUseCase(fileRepository)

    @Singleton
    @Provides
    fun provideMoveFileToPermanentUseCase(
        fileRepository: FileRepository
    ): MoveFileToPermanentUseCase = MoveFileToPermanentUseCase(fileRepository)

    @Singleton
    @Provides
    fun provideCacheDiaryImageUseCase(
        fileRepository: FileRepository
    ): CacheDiaryImageUseCase = CacheDiaryImageUseCase(fileRepository)
}
