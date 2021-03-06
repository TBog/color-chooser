/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.color.chooser.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.res.use

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object AttrUtils {
    @ColorInt
    fun resolveColor(
        context: Context,
        @AttrRes attr: Int,
        @ColorInt defaultColor: Int
    ): Int = resolveColor(context, 0, attr, defaultColor)

    @SuppressLint("Recycle")
    @ColorInt
    private fun resolveColor(
        context: Context,
        @StyleRes style: Int,
        @AttrRes attr: Int,
        @ColorInt defaultColor: Int
    ): Int = context.obtainStyledAttributes(style, intArrayOf(attr))
        .use { it.getColor(0, defaultColor) }
}
