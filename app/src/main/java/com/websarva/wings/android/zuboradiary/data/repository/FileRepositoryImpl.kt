package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.file.ImageFileDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.file.FileRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.file.toImageFileNameDataModel
import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import javax.inject.Inject

internal class FileRepositoryImpl @Inject constructor(
    private val imageFileDataSource: ImageFileDataSource
) : FileRepository {

    override fun buildImageFileAbsolutePathFromCache(fileName: DiaryImageFileName): String {
        return imageFileDataSource.buildImageFileAbsolutePathFromCache(fileName.toImageFileNameDataModel())
    }

    override fun buildImageFileAbsolutePathFromPermanent(fileName: DiaryImageFileName): String {
        return imageFileDataSource.buildImageFileAbsolutePathFromPermanent(fileName.toImageFileNameDataModel())
    }

    override suspend fun existsImageFileInCache(fileName: DiaryImageFileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInCache(fileName.toImageFileNameDataModel())
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun existsImageFileInPermanent(fileName: DiaryImageFileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInPermanent(fileName.toImageFileNameDataModel())
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun existsImageFileInBackup(fileName: DiaryImageFileName): Boolean {
        return try {
            imageFileDataSource.existsImageFileInBackup(fileName.toImageFileNameDataModel())
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun cacheImageFile(
        uriString: String,
        fileBaseName: String
    ): DiaryImageFileName {
        return try {
            val savedImageFileName =
                imageFileDataSource.cacheImageFile(uriString, fileBaseName)
            DiaryImageFileName(savedImageFileName)
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun moveImageFileToPermanent(fileName: DiaryImageFileName) {
        try {
            imageFileDataSource.moveImageFileToPermanent(fileName.toImageFileNameDataModel())
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun moveImageFileToBackup(fileName: DiaryImageFileName) {
        try {
            imageFileDataSource.moveImageFileToBackup(fileName.toImageFileNameDataModel())
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun restoreImageFileFromPermanent(fileName: DiaryImageFileName) {
        try {
            imageFileDataSource.restoreImageFileFromPermanent(fileName.toImageFileNameDataModel())
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun restoreImageFileFromBackup(fileName: DiaryImageFileName) {
        try {
            imageFileDataSource.restoreImageFileFromBackup(fileName.toImageFileNameDataModel())
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun deleteImageFileInPermanent(fileName: DiaryImageFileName) {
        try {
            imageFileDataSource.deleteImageFileInPermanent(fileName.toImageFileNameDataModel())
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun clearAllImageFilesInCache() {
        try {
            imageFileDataSource.deleteAllFilesInCache()
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun clearAllImageFilesInBackup() {
        try {
            imageFileDataSource.deleteAllFilesInBackup()
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun clearAllImageFiles() {
        try {
            imageFileDataSource.deleteAllFiles()
        } catch (e: Exception) {
            throw FileRepositoryExceptionMapper.toDomainException(e)
        }
    }
}
