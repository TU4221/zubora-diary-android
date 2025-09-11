package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.file.DeleteFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.file.MoveFileToPermanentUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.file.SaveImageFileUseCase
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
    fun provideDeleteFileUseCase(
        fileRepository: FileRepository
    ): DeleteFileUseCase = DeleteFileUseCase(fileRepository)

    @Singleton
    @Provides
    fun provideMoveFileToPermanentUseCase(
        fileRepository: FileRepository
    ): MoveFileToPermanentUseCase = MoveFileToPermanentUseCase(fileRepository)

    @Singleton
    @Provides
    fun provideSaveImageFileUseCase(
        fileRepository: FileRepository
    ): SaveImageFileUseCase = SaveImageFileUseCase(fileRepository)
}
