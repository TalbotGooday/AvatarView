package com.goodayapps.widget

import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntRange
import com.goodayapps.widget.utils.getGradientShader

class TextOverDrawable(
		private val size: Int,
		private val border: Border,
		private val text: Text,
) : Drawable() {

	private var textOverPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
		this.textSize = text.textSize
		this.color = text.textColor

		if (text.typeface != null) {
			this.typeface = text.typeface
		}

		this.style = Paint.Style.FILL

		this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
	}

	private val textOverBackgroundPaint = Paint().apply {
		this.isAntiAlias = true
		this.style = Paint.Style.STROKE
		this.strokeWidth = border.width.toFloat()
		this.shader = getGradientShader(
				colorStart = border.color,
				colorEnd = border.colorSecondary,
				gradientAngle = border.gradientAngle.toDouble(),
				size = size
		)
	}

	private var bufferBitmap: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
	private var bufferCanvas: Canvas = Canvas(bufferBitmap)

	private var circleRadius = 0f
	private var circleCenter = 0f
	private var backgroundCircleRadius = 0f

	init {
		circleRadius = (size / 2.0f)
		circleCenter = circleRadius
		backgroundCircleRadius = circleRadius - (border.width * .90f)
	}

	override fun draw(canvas: Canvas) {
		canvas.save()
		canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())

		drawTextOver()

		canvas.drawBitmap(bufferBitmap, Matrix(), null)
		canvas.restore()
	}

	override fun setAlpha(alpha: Int) {
	}

	override fun setColorFilter(colorFilter: ColorFilter?) {
	}

	override fun getOpacity(): Int {
		return PixelFormat.TRANSLUCENT
	}


	private fun drawTextOver() {
		val radius = circleRadius - border.width
		bufferCanvas.drawCircle(
				circleCenter,
				circleCenter,
				radius - (border.width / 2f),
				textOverBackgroundPaint
		)

		val path = Path()
		bufferCanvas.save()
		bufferCanvas.rotate((text.angle).toFloat(), circleCenter, circleCenter)
		path.addCircle(circleCenter, circleCenter, radius, Path.Direction.CCW)
		bufferCanvas.drawTextOnPath("#OPENTOWORK", path, 0f, 0f, textOverPaint)
		bufferCanvas.restore()
	}

	@TextOverDrawableDsl
	class Builder {
		var border = Border()
		var text = Text()

	}

	@TextOverDrawableDsl
	class Border(
			@Dimension(unit = Dimension.PX) val width: Int = 0,
			@ColorInt val color: Int = Color.parseColor("#FF51E400"),
			@ColorInt val colorSecondary: Int = color,
			val gradientAngle: Int = 0,
	) {
		class Builder {
			@Dimension(unit = Dimension.PX)
			var width: Int = 0
				private set

			@ColorInt
			var color: Int = Color.parseColor("#FF51E400")
				private set

			@ColorInt
			var colorSecondary: Int? = null
				private set

			var gradientAngle: Int = 0
				private set

			fun color(@ColorInt color: Int?) = apply { this.color = color ?: this.color }

			fun colorSecondary(@ColorInt color: Int?) = apply {
				this.colorSecondary = color ?: this.color
			}

			fun width(@Dimension(unit = Dimension.PX) width: Int?) = apply {
				this.width = width ?: 0
			}

			fun gradientAngle(@IntRange(from = 0, to = 360) angle: Int) = apply {
				this.gradientAngle = angle
			}

			fun build() = Border(width, color, colorSecondary ?: color, gradientAngle)
		}
	}

	@TextOverDrawableDsl
	class Text(
			var text: CharSequence? = "?",
			@ColorInt
			var textColor: Int = Color.WHITE,
			@Dimension(unit = Dimension.PX)
			var textSize: Float = 0f,
			var typeface: Typeface? = null,
			var angle: Int = 0,
	) {
		class Builder {
			var text: CharSequence? = "?"
				private set

			@ColorInt
			var color: Int = Color.WHITE
				private set

			@Dimension(unit = Dimension.PX)
			var size: Float = 0f
				private set

			var typeface: Typeface? = null
				private set

			var angle: Int = 0
				private set

			fun text(text: CharSequence?) = apply { this.text = text ?: "?" }

			fun color(@ColorInt color: Int?) = apply { this.color = color ?: this.color }

			fun size(@Dimension(unit = Dimension.PX) size: Float?) = apply {
				this.size = size ?: 0f
			}

			fun typeface(typeface: Typeface?) = apply { this.typeface = typeface }

			fun angle(@IntRange(from = 0, to = 360) angle: Int) = apply {
				this.angle = angle
			}

			fun build() = AvatarDrawable.Placeholder(text, color, size, typeface)
		}
	}
}