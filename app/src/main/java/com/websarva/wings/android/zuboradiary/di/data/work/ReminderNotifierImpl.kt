package com.websarva.wings.android.zuboradiary.di.data.work

import androidx.annotation.RequiresPermission
import com.websarva.wings.android.zuboradiary.data.worker.ReminderNotifier
import com.websarva.wings.android.zuboradiary.ui.reminder.ReminderNotificationManager
import javax.inject.Inject

internal class ReminderNotifierImpl @Inject constructor(
    private val reminderNotificationManager: ReminderNotificationManager
): ReminderNotifier {

    @RequiresPermission(value = "android.permission.POST_NOTIFICATIONS")
    override fun show() {
        reminderNotificationManager.show()
    }

}
