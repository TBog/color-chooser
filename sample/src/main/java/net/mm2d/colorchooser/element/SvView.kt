/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.colorchooser.element

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import net.mm2d.colorchooser.R
import net.mm2d.colorchooser.util.ColorUtils
import net.mm2d.colorchooser.util.clamp

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SvView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    @ColorInt
    private var color: Int = Color.BLACK
    private var maxColor: Int = Color.RED
    private var maskBitmap: Bitmap? = null
    private val paint = Paint().also {
        it.isAntiAlias = true
    }
    private val _padding = resources.getDimensionPixelOffset(R.dimen.panel_margin)
    private val _width = resources.getDimensionPixelOffset(R.dimen.hsv_size) + _padding * 2
    private val _height = resources.getDimensionPixelOffset(R.dimen.hsv_size) + _padding * 2
    private val _sampleRadius = resources.getDimension(R.dimen.sample_radius)
    private val _sampleFrameRadius = _sampleRadius + resources.getDimension(R.dimen.sample_frame)
    private val _sampleShadowRadius =
        _sampleFrameRadius + resources.getDimension(R.dimen.sample_shadow)
    private val maskRect = Rect(0, 0,
        TONE_SIZE,
        TONE_SIZE
    )
    private val targetRect = Rect()
    private var hue: Float = 0f
    var saturation: Float = 0f
        private set
    var value: Float = 0f
        private set
    private val colorSampleFrame = ContextCompat.getColor(context,
        R.color.sample_frame
    )
    private val colorSampleShadow = ContextCompat.getColor(context,
        R.color.sample_shadow
    )
    private val hsvCache = FloatArray(3)
    var onColorChanged: ((color: Int) -> Unit)? = null

    init {
        Thread {
            maskBitmap = createMaskBitmap()
            invalidate()
        }.start()
    }

    fun setColor(@ColorInt color: Int) {
        this.color = color
        ColorUtils.colorToHsv(color, hsvCache)
        updateHue(hsvCache[0])
        updateSv(hsvCache[1], hsvCache[2])
    }

    fun setHue(h: Float) {
        color = ColorUtils.hsvToColor(h, saturation, value)
        updateHue(h)
    }

    private fun updateHue(h: Float) {
        if (hue == h) {
            return
        }
        hue = h
        maxColor = ColorUtils.hsvToColor(hue, 1f, 1f)
        invalidate()
    }

    private fun updateSv(s: Float, v: Float, fromUser: Boolean = false) {
        if (saturation == s && value == v) {
            return
        }
        saturation = s
        value = v
        invalidate()
        if (fromUser) {
            onColorChanged?.invoke(color)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val s = ((event.x - targetRect.left) / targetRect.width()).clamp(0f, 1f)
        val v = ((targetRect.bottom - event.y) / targetRect.height()).clamp(0f, 1f)
        color = ColorUtils.hsvToColor(hue, s, v)
        updateSv(s, v, true)
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
        val mask = maskBitmap ?: return
        paint.color = maxColor
        canvas.drawRect(targetRect, paint)
        canvas.drawBitmap(mask, maskRect, targetRect, paint)
        val x = saturation * targetRect.width() + targetRect.left
        val y = (1f - value) * targetRect.height() + targetRect.top
        paint.color = colorSampleShadow
        canvas.drawCircle(x, y, _sampleShadowRadius, paint)
        paint.color = colorSampleFrame
        canvas.drawCircle(x, y, _sampleFrameRadius, paint)
        paint.color = color
        canvas.drawCircle(x, y, _sampleRadius, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val paddingHorizontal = paddingLeft + paddingRight
        val paddingVertical = paddingTop + paddingBottom
        val resizeWidth = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
        val resizeHeight = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY

        if (!resizeWidth && !resizeHeight) {
            setMeasuredDimension(
                resolveSizeAndState(
                    Math.max(_width + paddingHorizontal, suggestedMinimumWidth),
                    widthMeasureSpec,
                    MeasureSpec.UNSPECIFIED
                ),
                resolveSizeAndState(
                    Math.max(_height + paddingVertical, suggestedMinimumHeight),
                    heightMeasureSpec,
                    MeasureSpec.UNSPECIFIED
                )
            )
            return
        }

        var widthSize = resolveAdjustedSize(_width + paddingHorizontal, widthMeasureSpec)
        var heightSize = resolveAdjustedSize(_height + paddingVertical, heightMeasureSpec)
        val actualAspect =
            (widthSize - paddingHorizontal).toFloat() / (heightSize - paddingVertical)
        if (Math.abs(actualAspect - 1f) < 0.0000001) {
            setMeasuredDimension(widthSize, heightSize)
            return
        }
        if (resizeWidth) {
            val newWidth = heightSize - paddingVertical + paddingHorizontal
            if (!resizeHeight) {
                widthSize = resolveAdjustedSize(newWidth, widthMeasureSpec)
            }
            if (newWidth <= widthSize) {
                widthSize = newWidth
                setMeasuredDimension(widthSize, heightSize)
                return
            }
        }
        if (resizeHeight) {
            val newHeight = widthSize - paddingHorizontal + paddingVertical
            if (!resizeWidth) {
                heightSize = resolveAdjustedSize(newHeight, heightMeasureSpec)
            }
            if (newHeight <= heightSize) {
                heightSize = newHeight
            }
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    private fun resolveAdjustedSize(desiredSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            MeasureSpec.UNSPECIFIED -> desiredSize
            MeasureSpec.AT_MOST -> Math.min(desiredSize, specSize)
            MeasureSpec.EXACTLY -> specSize
            else -> desiredSize
        }
    }

    companion object {
        private const val TONE_MAX = 255f
        private const val TONE_SIZE = 256

        private fun createMaskBitmap(): Bitmap {
            val pixels = IntArray(TONE_SIZE * TONE_SIZE)
            for (y in 0 until TONE_SIZE) {
                for (x in 0 until TONE_SIZE) {
                    pixels[x + y * TONE_SIZE] =
                            ColorUtils.svToMask(x / TONE_MAX, (TONE_MAX - y) / TONE_MAX)
                }
            }
            return Bitmap.createBitmap(
                pixels,
                0,
                TONE_SIZE,
                TONE_SIZE,
                TONE_SIZE,
                Bitmap.Config.ARGB_8888
            )
        }
    }
}
