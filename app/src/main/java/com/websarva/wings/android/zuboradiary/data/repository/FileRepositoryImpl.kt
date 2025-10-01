package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.file.ImageFileDataSource
import com.websarva.wings.android.zuboradiary.data.file.exception.FileOperationException
import com.websarva.wings.android.zuboradiary.data.mapper.file.FileRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.file.toImageFileNameDataModel
import com.websarva.wings.android.zuboradiary.domain.model.FileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository

internal class FileRepositoryImpl(
    private val imageFileDataSource: ImageFileDataSource,
    private val fileRepositoryExceptionMapper: FileRepositoryExceptionMapper
) : FileRepository {

    override fun buildImageFileAbsolutePathFromCache(fileName: FileName): String {
        return imageFileDataSource.buildImageFileAbsolutePathFromCache(fileName.toImageFileNameDataModel())
    }

    override fun buildImageFileAbsolutePathFromPermanent(fileName: FileName): String {
        return imageFileDataSource.buildImageFileAbsolutePathFromPermanent(fileName.toImageFileNameDataModel())
    }

    override suspend fun existsImageFileInCache(fileName: FileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInCache(fileName.toImageFileNameDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun existsImageFileInPermanent(fileName: FileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInPermanent(fileName.toImageFileNameDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun existsImageFileInBackup(fileName: FileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInBackup(fileName.toImageFileNameDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun cacheImageFile(
        uriString: String,
        fileBaseName: String
    ): FileName {
        return try {
            val savedImageFileName =
                imageFileDataSource.cacheImageFile(uriString, fileBaseName)
            FileName(savedImageFileName)
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun moveImageFileToPermanent(fileName: FileName) {
        try {
            imageFileDataSource.moveImageFileToPermanent(fileName.toImageFileNameDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun moveImageFileToBackup(fileName: FileName) {
        try {
            imageFileDataSource.moveImageFileToBackup(fileName.toImageFileNameDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun restoreImageFileFromPermanent(fileName: FileName) {
        try {
            imageFileDataSource.restoreImageFileFromPermanent(fileName.toImageFileNameDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun restoreImageFileFromBackup(fileName: FileName) {
        try {
            imageFileDataSource.restoreImageFileFromBackup(fileName.toImageFileNameDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun deleteImageFileInPermanent(fileName: FileName) {
        try {
            imageFileDataSource.deleteImageFileInPermanent(fileName.toImageFileNameDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun clearAllImageFilesInCache() {
        try {
            imageFileDataSource.deleteAllFilesInCache()
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun clearAllImageFilesInBackup() {
        try {
            imageFileDataSource.deleteAllFilesInBackup()
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun clearAllImageFiles() {
        try {
            imageFileDataSource.deleteAllFiles()
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }
}
