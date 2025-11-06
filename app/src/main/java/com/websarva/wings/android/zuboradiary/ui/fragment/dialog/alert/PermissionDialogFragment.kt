package com.websarva.wings.android.zuboradiary.ui.fragment.dialog.alert

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.fragment.dialog.setResult
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

class PermissionDialogFragment : BaseAlertDialogFragment() {

    override fun createTitle(): String {
        return getString(R.string.dialog_permission_title)
    }

    override fun createMessage(): String {
        val firstMessage = getString(R.string.dialog_permission_first_message)
        val secondMessage =
            PermissionDialogFragmentArgs.fromBundle(requireArguments()).permissionName
        val thirdMessage = getString(R.string.dialog_permission_third_message)
        return firstMessage + secondMessage + thirdMessage
    }

    override fun handleOnPositiveButtonClick() {
        setResult(RESULT_KEY, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(RESULT_KEY, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(RESULT_KEY, DialogResult.Cancel)
    }

    companion object {
        val RESULT_KEY = RESULT_KEY_PREFIX + PermissionDialogFragment::class.java.name
    }
}
