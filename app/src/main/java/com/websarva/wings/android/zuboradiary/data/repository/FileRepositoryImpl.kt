package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.file.ImageFileDataSource
import com.websarva.wings.android.zuboradiary.data.file.exception.FileOperationException
import com.websarva.wings.android.zuboradiary.data.mapper.file.FileRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.file.toDataModel
import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository

internal class FileRepositoryImpl(
    private val imageFileDataSource: ImageFileDataSource,
    private val fileRepositoryExceptionMapper: FileRepositoryExceptionMapper
) : FileRepository {

    override fun buildImageFileAbsolutePathFromCache(fileName: ImageFileName): String {
        return imageFileDataSource.buildImageFileAbsolutePathFromCache(fileName.toDataModel())
    }

    override fun buildImageFileAbsolutePathFromPermanent(fileName: ImageFileName): String {
        return imageFileDataSource.buildImageFileAbsolutePathFromPermanent(fileName.toDataModel())
    }

    override suspend fun existsImageFileInCache(fileName: ImageFileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInCache(fileName.toDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun existsImageFileInPermanent(fileName: ImageFileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInPermanent(fileName.toDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun existsImageFileInBackup(fileName: ImageFileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInBackup(fileName.toDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun cacheImageFile(
        uriString: String,
        fileBaseName: String
    ): ImageFileName {
        return try {
            val savedImageFileName =
                imageFileDataSource.cacheImageFile(uriString, fileBaseName)
            ImageFileName(savedImageFileName)
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun moveImageFileToPermanent(fileName: ImageFileName) {
        try {
            imageFileDataSource.moveImageFileToPermanent(fileName.toDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun moveImageFileToBackup(fileName: ImageFileName) {
        try {
            imageFileDataSource.moveImageFileToBackup(fileName.toDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun restoreImageFileFromPermanent(fileName: ImageFileName) {
        try {
            imageFileDataSource.restoreImageFileFromPermanent(fileName.toDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun restoreImageFileFromBackup(fileName: ImageFileName) {
        try {
            imageFileDataSource.restoreImageFileFromBackup(fileName.toDataModel())
        } catch (e: FileOperationException) {
            throw fileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun deleteImageFileInPermanent(fileName: ImageFileName) {
        try {
            imageFileDataSource.deleteImageFileInPermanent(fileName.toDataModel())
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
