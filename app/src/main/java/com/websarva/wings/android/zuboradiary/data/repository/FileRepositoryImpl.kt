package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.file.ImageFileDataSource
import com.websarva.wings.android.zuboradiary.data.file.exception.FileOperationException
import com.websarva.wings.android.zuboradiary.data.mapper.file.FileRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FileRepositoryImpl(
    private val imageFileDataSource: ImageFileDataSource,
    private val fileRepositoryExceptionMapper: FileRepositoryExceptionMapper
) : FileRepository {

    override suspend fun buildImageFileAbsolutePathFromCache(fileName: ImageFileName): String {
        return withContext(Dispatchers.IO) {
            imageFileDataSource.buildImageFileAbsolutePathFromCache(fileName.fullName)
        }
    }

    override suspend fun buildImageFileAbsolutePathFromPermanent(fileName: ImageFileName): String {
        return withContext(Dispatchers.IO) {
            imageFileDataSource.buildImageFileAbsolutePathFromPermanent(fileName.fullName)
        }
    }

    override suspend fun existsImageFileInCache(fileName: ImageFileName): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.existsImageFileInCache(fileName.fullName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun existsImageFileInPermanent(fileName: ImageFileName): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.existsImageFileInPermanent(fileName.fullName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun existsImageFileInBackup(fileName: ImageFileName): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.existsImageFileInBackup(fileName.fullName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun cacheImageFile(
        uriString: String,
        fileBaseName: String
    ): ImageFileName {
        return withContext(Dispatchers.IO) {
            try {
                val savedImageFileName =
                    imageFileDataSource.cacheImageFile(uriString, fileBaseName)
                ImageFileName(savedImageFileName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun moveImageFileToPermanent(fileName: ImageFileName) {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.moveImageFileToPermanent(fileName.fullName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun moveImageFileToBackup(fileName: ImageFileName) {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.moveImageFileToBackup(fileName.fullName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun restoreImageFileFromPermanent(fileName: ImageFileName) {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.restoreImageFileFromPermanent(fileName.fullName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun restoreImageFileFromBackup(fileName: ImageFileName) {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.restoreImageFileFromBackup(fileName.fullName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun deleteImageFileInPermanent(fileName: ImageFileName) {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.deleteImageFileInPermanent(fileName.fullName)
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun clearAllImageFilesInCache() {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.deleteAllFilesInCache()
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun clearAllImageFilesInBackup() {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.deleteAllFilesInBackup()
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }

    override suspend fun clearAllImageFiles() {
        return withContext(Dispatchers.IO) {
            try {
                imageFileDataSource.deleteAllFiles()
            } catch (e: FileOperationException) {
                throw fileRepositoryExceptionMapper.toRepositoryException(e)
            }
        }
    }
}
