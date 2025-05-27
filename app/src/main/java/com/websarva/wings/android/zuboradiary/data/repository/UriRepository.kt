package com.websarva.wings.android.zuboradiary.data.repository

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class UriRepository (
    private val dataSource: UriPermissionDataSource,
    private val diaryDao: DiaryDAO
) {
    fun takePersistablePermission(uri: Uri) {
        dataSource.takePersistablePermission(uri)
    }

    suspend fun releasePersistablePermission(uri: Uri) {
        val existsPicturePath =
            withContext(Dispatchers.IO) {
                diaryDao.existsPicturePath(uri.toString())
            }
        if (existsPicturePath) return

        dataSource.releasePersistablePermission(uri)
    }

    fun releaseAllPersistablePermission() {
        dataSource.releaseAllPersistablePermission()
    }
}
