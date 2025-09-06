package com.websarva.wings.android.zuboradiary.di.data.worker

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.worker.ReminderNotificationMessageProvider

internal class ReminderNotificationMessageProviderImpl(
    private val context: Context
): ReminderNotificationMessageProvider {

    override val title
        get() = context.getString(R.string.reminder_notification_worker_content_title)

    override val text
        get() = context.getString(R.string.reminder_notification_worker_content_text)
}
