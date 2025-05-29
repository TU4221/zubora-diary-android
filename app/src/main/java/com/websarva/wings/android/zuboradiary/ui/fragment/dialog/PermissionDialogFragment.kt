package com.websarva.wings.android.zuboradiary.ui.fragment.dialog

import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.fragment.RESULT_KEY_PREFIX
import com.websarva.wings.android.zuboradiary.ui.model.result.DialogResult

class PermissionDialogFragment : BaseAlertDialogFragment() {

    companion object {
        @JvmField
        val KEY_RESULT = RESULT_KEY_PREFIX + PermissionDialogFragment::class.java.name
    }

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
        setResult(KEY_RESULT, DialogResult.Positive(Unit))
    }

    override fun handleOnNegativeButtonClick() {
        setResult(KEY_RESULT, DialogResult.Negative)
    }

    override fun handleOnCancel() {
        setResult(KEY_RESULT, DialogResult.Cancel)
    }
}
