package com.websarva.wings.android.zuboradiary.data.usecase.uri

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ReleaseUriPermissionUseCase(
    private val uriRepository: UriRepository,
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(uri: Uri?) {
        if (uri == null) return

        val existsPicturePath =
            withContext(Dispatchers.IO) {
                diaryRepository.existsPicturePath(uri)
            }
        if (existsPicturePath) return

        uriRepository.releasePersistablePermission(uri)
    }
}
