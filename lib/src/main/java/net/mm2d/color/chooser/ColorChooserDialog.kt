/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.color.chooser

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Color chooser dialog
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ColorChooserDialog : DialogFragment() {
    private lateinit var dialogView: DialogView
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val color = arguments?.getInt(KEY_INITIAL_COLOR, 0) ?: 0
        dialogView = DialogView(activity!!)
        dialogView.onChange(color, null)
        return AlertDialog.Builder(activity!!, theme)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                notifySelect()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface?) {
        val requestCode = arguments?.getInt(KEY_REQUEST_CODE) ?: return
        extractCallback()?.onColorChooserResult(requestCode, Activity.RESULT_CANCELED, 0)
    }

    private fun notifySelect() {
        val requestCode = arguments?.getInt(KEY_REQUEST_CODE) ?: return
        extractCallback()?.onColorChooserResult(requestCode, Activity.RESULT_OK, dialogView.color)
    }

    private fun extractCallback(): Callback? {
        return targetFragment as? Callback ?: activity as? Callback
    }

    /**
     * Result callback implements to Fragment or Activity
     */
    interface Callback {
        /**
         * Call at close dialog
         *
         * @param requestCode requestCode of show parameter
         * @param resultCode `Activity.RESULT_OK` or `Activity.RESULT_CANCELED`
         * @param color selected color
         */
        fun onColorChooserResult(requestCode: Int, resultCode: Int, color: Int)
    }

    companion object {
        private const val KEY_INITIAL_COLOR = "KEY_INITIAL_COLOR"
        private const val KEY_REQUEST_CODE = "KEY_REQUEST_CODE"
        private const val TAG = "ColorChooserDialog"

        /**
         * @param activity FragmentActivity
         * @param requestCode use in listener call
         * @param initialColor initial color
         */
        fun show(
            activity: FragmentActivity,
            requestCode: Int = 0,
            initialColor: Int = Color.WHITE
        ) {
            val fragmentManager = activity.supportFragmentManager ?: return
            if (fragmentManager.findFragmentByTag(TAG) != null) {
                return
            }
            val arguments = Bundle().apply {
                putInt(KEY_INITIAL_COLOR, initialColor)
                putInt(KEY_REQUEST_CODE, requestCode)
            }
            ColorChooserDialog().also {
                it.arguments = arguments
                it.show(fragmentManager, TAG)
            }
        }

        /**
         * @param fragment Fragment
         * @param requestCode use in listener call
         * @param initialColor initial color
         */
        fun show(
            fragment: Fragment,
            requestCode: Int = 0,
            initialColor: Int = Color.WHITE
        ) {
            val fragmentManager = fragment.fragmentManager ?: return
            if (fragmentManager.findFragmentByTag(TAG) != null) {
                return
            }
            val arguments = Bundle().apply {
                putInt(KEY_INITIAL_COLOR, initialColor)
                putInt(KEY_REQUEST_CODE, requestCode)
            }
            ColorChooserDialog().also {
                it.setTargetFragment(fragment, requestCode)
                it.arguments = arguments
                it.show(fragmentManager, TAG)
            }
        }
    }
}