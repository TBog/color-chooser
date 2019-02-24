/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.color.chooser.element

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import net.mm2d.color.chooser.R
import kotlin.math.max

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class PreviewView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().also {
        it.isAntiAlias = true
    }
    private val _width = resources.getDimensionPixelOffset(R.dimen.mm2d_cc_preview_width)
    private val _height = resources.getDimensionPixelOffset(R.dimen.mm2d_cc_preview_height)
    private val frameLineWidth = resources.getDimension(R.dimen.mm2d_cc_sample_frame)
    private val shadowLineWidth = resources.getDimension(R.dimen.mm2d_cc_sample_shadow)
    private val colorSampleFrame = ContextCompat.getColor(
        context,
        R.color.mm2d_cc_sample_frame
    )
    private val colorSampleShadow = ContextCompat.getColor(
        context,
        R.color.mm2d_cc_sample_shadow
    )
    private val checkerRect = Rect()
    private val targetRect = Rect()
    private val checkerSize = resources.getDimensionPixelSize(R.dimen.mm2d_cc_checker_size)
    private val colorCheckerLight = ContextCompat.getColor(context, R.color.mm2d_cc_checker_light)
    private val colorCheckerDark = ContextCompat.getColor(context, R.color.mm2d_cc_checker_dark)
    private var checker: Bitmap? = null
    var color: Int = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        paint.style = Style.STROKE
        paint.color = colorSampleShadow
        paint.strokeWidth = shadowLineWidth
        val shadow = frameLineWidth + shadowLineWidth / 2
        canvas.drawRect(
            targetRect.left - shadow,
            targetRect.top - shadow,
            targetRect.right + shadow,
            targetRect.bottom + shadow,
            paint
        )
        paint.color = colorSampleFrame
        paint.strokeWidth = frameLineWidth
        val frame = frameLineWidth / 2
        canvas.drawRect(
            targetRect.left - frame,
            targetRect.top - frame,
            targetRect.right + frame,
            targetRect.bottom + frame,
            paint
        )
        val checker = checker ?: return
        canvas.drawBitmap(checker, checkerRect, targetRect, paint)
        paint.style = Style.FILL
        paint.color = color
        canvas.drawRect(targetRect, paint)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val border = (frameLineWidth + shadowLineWidth).toInt()
        targetRect.set(
            paddingLeft + border,
            paddingTop + border,
            width - paddingRight - border,
            height - paddingBottom - border
        )
        checkerRect.set(0, 0, targetRect.width(), targetRect.height())
        checker = createChecker(
            checkerSize,
            checkerRect.width(),
            checkerRect.height(),
            colorCheckerLight,
            colorCheckerDark
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSizeAndState(
                max(_width, suggestedMinimumWidth),
                widthMeasureSpec,
                MeasureSpec.UNSPECIFIED
            ),
            resolveSizeAndState(
                max(_height, suggestedMinimumHeight),
                heightMeasureSpec,
                MeasureSpec.UNSPECIFIED
            )
        )
    }

    companion object {
        private fun createChecker(
            checkerSize: Int,
            width: Int,
            height: Int,
            lightColor: Int,
            darkColor: Int
        ): Bitmap {
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[x + y * width] =
                        if ((x / checkerSize + y / checkerSize) % 2 == 0) lightColor else darkColor
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888)
        }
    }
}