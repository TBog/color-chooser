/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.color.chooser.element

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import net.mm2d.color.chooser.R
import net.mm2d.color.chooser.util.setAlpha
import net.mm2d.color.chooser.util.toOpacity
import kotlin.math.max

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal class ColorSliderView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().also {
        it.isAntiAlias = true
    }
    private val _padding = resources.getDimensionPixelOffset(R.dimen.mm2d_cc_panel_margin)
    private val _width =
        resources.getDimensionPixelOffset(R.dimen.mm2d_cc_slider_width) + _padding * 2
    private val _height =
        resources.getDimensionPixelOffset(R.dimen.mm2d_cc_slider_height) + _padding * 2
    private val _sampleRadius = resources.getDimension(R.dimen.mm2d_cc_sample_radius)
    private val _sampleFrameRadius =
        _sampleRadius + resources.getDimension(R.dimen.mm2d_cc_sample_frame)
    private val _sampleShadowRadius =
        _sampleFrameRadius + resources.getDimension(R.dimen.mm2d_cc_sample_shadow)
    private val frameLineWidth = resources.getDimension(R.dimen.mm2d_cc_sample_frame)
    private val shadowLineWidth = resources.getDimension(R.dimen.mm2d_cc_sample_shadow)
    private val gradationRect = Rect(0, 0, RANGE, 1)
    private val targetRect = Rect()
    private val colorSampleFrame = ContextCompat.getColor(
        context,
        R.color.mm2d_cc_sample_frame
    )
    private val colorSampleShadow = ContextCompat.getColor(
        context,
        R.color.mm2d_cc_sample_shadow
    )
    private var checker: Bitmap? = null
    private var _value: Float = 0f
    private var maxColor: Int
    private var gradation: Bitmap
    private val baseColor: Int
    private val alphaMode: Boolean
    var onValueChanged: ((value: Int, fromUser: Boolean) -> Unit)? = null
    val value: Int
        get() = (_value * MAX).toInt()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorSliderView)
        maxColor = a.getColor(R.styleable.ColorSliderView_maxColor, Color.WHITE)
        baseColor = a.getColor(R.styleable.ColorSliderView_baseColor, Color.BLACK)
        alphaMode = a.getBoolean(R.styleable.ColorSliderView_alphaMode, true)
        a.recycle()
        gradation = createGradation(maxColor)
        updateChecker()
    }

    fun setMaxColor(maxColor: Int) {
        this.maxColor = maxColor.toOpacity()
        gradation = createGradation(this.maxColor)
        invalidate()
    }

    fun setValue(value: Int) {
        _value = (value / MAX.toFloat()).coerceIn(0f, 1f)
        onValueChanged?.invoke(value, false)
        invalidate()
    }

    private fun updateChecker() {
        checker = if (alphaMode) {
            createChecker(
                resources.getDimensionPixelSize(R.dimen.mm2d_cc_checker_size),
                resources.getDimensionPixelSize(R.dimen.mm2d_cc_slider_height),
                ContextCompat.getColor(context, R.color.mm2d_cc_checker_light),
                ContextCompat.getColor(context, R.color.mm2d_cc_checker_dark)
            )
        } else {
            null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        _value = ((event.x - targetRect.left) / targetRect.width().toFloat()).coerceIn(0f, 1f)
        onValueChanged?.invoke(value, true)
        invalidate()
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        targetRect.set(
            paddingLeft + _padding,
            paddingTop + _padding,
            width - paddingRight - _padding,
            height - paddingBottom - _padding
        )
    }

    override fun onDraw(canvas: Canvas) {
        paint.style = Style.STROKE
        paint.color = colorSampleShadow
        paint.strokeWidth = shadowLineWidth
        val shadow = frameLineWidth + shadowLineWidth / 2
        canvas.drawRectFrame(targetRect, shadow, paint)
        paint.color = colorSampleFrame
        paint.strokeWidth = frameLineWidth
        val frame = frameLineWidth / 2
        canvas.drawRectFrame(targetRect, frame, paint)
        paint.style = Style.FILL
        if (alphaMode) {
            val checker = checker ?: return
            canvas.save()
            canvas.clipRect(targetRect)
            val top = targetRect.top.toFloat()
            for (left in targetRect.left until targetRect.right step checker.width) {
                canvas.drawBitmap(checker, left.toFloat(), top, paint)
            }
            canvas.restore()
        } else {
            paint.color = baseColor
            canvas.drawRect(targetRect, paint)
        }
        canvas.drawBitmap(gradation, gradationRect, targetRect, paint)
        val x = _value * targetRect.width() + targetRect.left
        val y = targetRect.centerY().toFloat()
        paint.color = colorSampleShadow
        canvas.drawCircle(x, y, _sampleShadowRadius, paint)
        paint.color = colorSampleFrame
        canvas.drawCircle(x, y, _sampleFrameRadius, paint)
        paint.color = baseColor
        canvas.drawCircle(x, y, _sampleRadius, paint)
        paint.color = maxColor.setAlpha(value)
        canvas.drawCircle(x, y, _sampleRadius, paint)
    }

    private fun Canvas.drawRectFrame(rect: Rect, offset: Float, paint: Paint) {
        drawRect(
            rect.left - offset,
            rect.top - offset,
            rect.right + offset,
            rect.bottom + offset,
            paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSizeAndState(
                max(_width + paddingLeft + paddingRight, suggestedMinimumWidth),
                widthMeasureSpec,
                MeasureSpec.UNSPECIFIED
            ),
            resolveSizeAndState(
                max(_height + paddingTop + paddingBottom, suggestedMinimumHeight),
                heightMeasureSpec,
                MeasureSpec.UNSPECIFIED
            )
        )
    }

    companion object {
        private const val MAX = 255
        private const val RANGE = 256

        private fun createGradation(color: Int): Bitmap {
            val pixels = IntArray(RANGE) { color.setAlpha(it) }
            return Bitmap.createBitmap(pixels, 0, RANGE, RANGE, 1, Bitmap.Config.ARGB_8888)
        }

        private fun createChecker(
            checkerSize: Int,
            height: Int,
            lightColor: Int,
            darkColor: Int
        ): Bitmap {
            val width = checkerSize * 4
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
