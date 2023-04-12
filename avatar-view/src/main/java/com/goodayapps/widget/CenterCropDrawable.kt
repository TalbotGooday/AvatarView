package com.goodayapps.widget

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.IntRange
import kotlin.math.roundToInt

class CenterCropDrawable(private val target: Drawable) : Drawable() {
    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        val sourceRect = RectF(
            0f, 0f, target.intrinsicWidth.toFloat(), target.intrinsicHeight
                .toFloat()
        )
        val screenRect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        val matrix = Matrix()
        matrix.setRectToRect(screenRect, sourceRect, Matrix.ScaleToFit.CENTER)
        val inverse = Matrix()
        matrix.invert(inverse)
        inverse.mapRect(sourceRect)
        target.setBounds(
            sourceRect.left.roundToInt(), sourceRect.top.roundToInt(),
            sourceRect.right.roundToInt(), sourceRect.bottom.roundToInt()
        )
        super.setBounds(left, top, right, bottom)
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.clipRect(bounds)
        try {
            target.draw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        canvas.restore()
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        target.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        target.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return target.opacity
    }
}