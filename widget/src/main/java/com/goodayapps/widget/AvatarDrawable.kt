package com.goodayapps.widget

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.goodayapps.widget.utils.getGradientShader

@AvatarDrawableDsl
class AvatarDrawable private constructor(
		private val size: Int,
		private val backgroundColor: Int = Color.BLACK,
		private val avatarDrawable: Drawable?,
		private val iconDrawableScale: Float = .5f,
		private val volumetricType: Volumetric = Volumetric.ALL,
		private val avatarMargin: Int = 0,
		private val border: Border,
		private val placeholder: Placeholder,
) : Drawable() {
	private var textLayout: StaticLayout? = null

	private var placeholderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
		this.textSize = placeholder.textSize
		this.color = placeholder.textColor

		if (placeholder.typeface != null) {
			this.typeface = placeholder.typeface
		}

		this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
	}

	private var avatarBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		this.color = backgroundColor
		this.isAntiAlias = true
		this.style = Paint.Style.FILL
	}

	private val gradientPaint = Paint().apply {
		this.isAntiAlias = true
		this.shader = LinearGradient(
				0f,
				size * .94f,
				0f,
				0f,
				0,
				Color.argb(102, 255, 255, 255),
				Shader.TileMode.CLAMP
		)
	}

	private val borderPaint = Paint().apply {
		this.isAntiAlias = true
		this.style = Paint.Style.STROKE
		this.strokeCap = Paint.Cap.ROUND
		this.strokeWidth = border.width.toFloat()
		this.shader = getGradientShader(
				colorStart = border.color,
				colorEnd = border.colorSecondary,
				gradientAngle = border.gradientAngle.toDouble(),
				size = size
		)
	}

	private val clipPaint = Paint().apply {
		this.color = Color.WHITE
		this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
		this.style = Paint.Style.FILL_AND_STROKE
	}

	private var bufferBitmap: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
	private var bufferCanvas: Canvas = Canvas(bufferBitmap)

	private var textWidth = 0f
	private var textHeight = 0f
	private var textLeft = 0f

	private var circleRadius = 0f
	private var circleCenter = 0f
	private var backgroundCircleRadius = 0f
	private val arcBorderRect = RectF()
	private val borderRect = RectF()

	constructor(builder: Builder) : this(
			size = builder.size,
			backgroundColor = builder.backgroundColor,
			avatarDrawable = builder.avatarDrawable,
			iconDrawableScale = builder.iconDrawableScale,
			volumetricType = builder.volumetricType,
			avatarMargin = builder.avatarMargin.coerceAtMost(builder.size / 2),
			border = builder.border,
			placeholder = builder.placeholder
	)

	init {
		circleRadius = (size / 2.0f)
		circleCenter = circleRadius
		backgroundCircleRadius = circleRadius - avatarMargin - (border.width * .90f)

		textLayout = placeholder.text?.let {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				StaticLayout.Builder.obtain(it, 0, it.length, placeholderPaint, size)
						.setAlignment(Layout.Alignment.ALIGN_CENTER)
						.setLineSpacing(1f, 1f)
						.setIncludePad(false)
						.build()
			} else {
				StaticLayout(
						it,
						placeholderPaint,
						size,
						Layout.Alignment.ALIGN_CENTER,
						1f,
						1f,
						false
				)
			}
		}?.also {
			if (it.lineCount > 0) {
				textLeft = it.getLineLeft(0)
				textWidth = it.getLineWidth(0)
				textHeight = it.getLineBottom(it.lineCount - 1).toFloat()
			}
		}

		borderRect.set(calculateBounds())
		arcBorderRect.apply {
			set(borderRect)
			inset(border.width / 2f, border.width / 2f)
		}
	}

	private fun calculateBounds(): RectF {
		val availableWidth = size
		val availableHeight = size

		val sideLength = availableWidth.coerceAtMost(availableHeight)

		val left = (availableWidth - sideLength) / 2f
		val top = (availableHeight - sideLength) / 2f

		return RectF(left, top, left + sideLength, top + sideLength)
	}

	private var iconColorFilter: ColorFilter? = null

	override fun draw(canvas: Canvas) {
		canvas.save()
		canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())

		var isIconDrawable = false

		val avatarBitmap: Bitmap? = when (avatarDrawable) {
			is BitmapDrawable -> {
				val bitmapSize = size - avatarMargin

				ThumbnailUtils.extractThumbnail(avatarDrawable.bitmap, bitmapSize, bitmapSize)
			}
			is Drawable -> {
				isIconDrawable = true
				val width = (size * iconDrawableScale).toInt()
				val height = (size * iconDrawableScale).toInt()

				if (width > 0 && height > 0) {
					Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
						avatarDrawable.setBounds(0, 0, width, height)

						iconColorFilter?.let { filter -> avatarDrawable.colorFilter = filter }

						avatarDrawable.draw(Canvas(it))
					}
				} else {
					null
				}
			}
			else -> {
				null
			}
		}

		//Drawing a background circle and a clip circle for the avatar
		bufferCanvas.drawCircle(circleCenter, circleCenter, backgroundCircleRadius, avatarBackgroundPaint)

		if (avatarBitmap == null) {
			drawPlaceholder()
		} else {
			drawBitmap(isIconDrawable, avatarBitmap)
		}

		//Draw volumetric gradient
		drawVolume(avatarBitmap != null)

		//Draw Border
		drawBorder()

		canvas.drawBitmap(bufferBitmap, Matrix(), null)
		canvas.restore()
	}

	private val totalArchesDegreeArea
		get() = border.archesDegreeArea.toFloat()

	private val animationLoopDegrees
		get() = border.archesAngle.toFloat()

	private var animationArchesSparseness = 1f

	private val individualArcDegreeLength
		get() = totalArchesDegreeArea / ((border.archesCount * 2) + 1)

	private val spaceBetweenArches
		get() = calculateSpaceBetweenArches()

	private val currentAnimationArchesArea
		get() = animationArchesSparseness * totalArchesDegreeArea

	private fun drawBorder() {
		if (border.width > 0) {
			if (border.archesCount > 1 && totalArchesDegreeArea > 0f) {
				drawArcBorder()
			} else {
				drawCircleBorder()
			}
		}
	}

	private fun drawArcBorder() {
		val totalDegrees = ((270f + animationLoopDegrees) % 360)
		val startSpace = if (totalArchesDegreeArea == 360f) 0f else individualArcDegreeLength
		drawArches(totalDegrees + startSpace, bufferCanvas)
		val startOfMainArch = totalDegrees + currentAnimationArchesArea
		bufferCanvas.drawArc(arcBorderRect, startOfMainArch, 360f - currentAnimationArchesArea, false, borderPaint)
	}

	private fun drawCircleBorder() {
		bufferCanvas.drawCircle(
				circleCenter,
				circleCenter,
				circleRadius - (border.width / 2f),
				borderPaint
		)
	}

	private fun drawArches(totalDegrees: Float, canvas: Canvas) {
		for (i in 0 until border.archesCount) {
			val arcDeg = (individualArcDegreeLength + spaceBetweenArches) * i
			val deg = totalDegrees + arcDeg// * animationArchesSparseness
			canvas.drawArc(arcBorderRect, deg, individualArcDegreeLength, false, borderPaint)
		}
	}

	override fun setAlpha(alpha: Int) {
	}

	override fun setColorFilter(colorFilter: ColorFilter?) {
		iconColorFilter = colorFilter
		invalidateSelf()
	}

	override fun getColorFilter(): ColorFilter? = iconColorFilter

	override fun getOpacity(): Int {
		return PixelFormat.TRANSLUCENT
	}

	private fun drawBitmap(isIconDrawable: Boolean, avatarBitmap: Bitmap) {
		if (isIconDrawable) {
			//Draw background

			val left = ((size - avatarBitmap.width) / 2f)
			val top = ((size - avatarBitmap.height) / 2f)

			bufferCanvas.drawBitmap(avatarBitmap, left, top, clipPaint)
		} else {
			bufferCanvas.drawBitmap(avatarBitmap, Matrix(), clipPaint)
		}
	}

	private fun drawPlaceholder() {
		//Draw text
		if (textLayout != null) {
			bufferCanvas.save()
			bufferCanvas.translate((size - textWidth) / 2 - textLeft, (size - textHeight) / 2)
			textLayout?.draw(bufferCanvas)
			bufferCanvas.restore()
		}
	}

	private fun drawVolume(isDrawable: Boolean) {
		if (volumetricType == Volumetric.NONE) return
		if (volumetricType == Volumetric.DRAWABLE && isDrawable.not()) return
		if (volumetricType == Volumetric.PLACEHOLDER && isDrawable) return

		bufferCanvas.drawCircle(circleCenter, circleCenter, backgroundCircleRadius, gradientPaint)
	}

	private fun calculateSpaceBetweenArches() =
			(totalArchesDegreeArea - (border.archesCount * individualArcDegreeLength)) /
					(border.archesCount + if (totalArchesDegreeArea == 360f) 0 else 1)

	@AvatarDrawableDsl
	class Builder {
		@Dimension(unit = Dimension.PX)
		var size: Int = 0
			private set

		@Dimension(unit = Dimension.PX)
		var avatarMargin: Int = 0
			private set

		@ColorInt
		var backgroundColor: Int = Color.parseColor("#4D4D7F")
			private set
		var avatarDrawable: Drawable? = null
			private set
		var iconDrawableScale: Float = .5f
			private set
		var volumetricType: Volumetric = Volumetric.ALL
			private set
		var border = Border()
		var placeholder = Placeholder()

		fun drawable(drawable: Drawable?) = apply { this.avatarDrawable = drawable }

		fun iconDrawableScale(@FloatRange(from = 0.0) scale: Float) = apply { this.iconDrawableScale = scale }

		fun size(@Dimension(unit = Dimension.PX) size: Int) = apply { this.size = size }

		fun avatarMargin(@Dimension(unit = Dimension.PX) margin: Int) = apply { this.avatarMargin = margin }

		fun volumetric(type: Volumetric) = apply { this.volumetricType = type }

		fun backgroundColor(@ColorInt color: Int?) = apply {
			this.backgroundColor = color ?: backgroundColor
		}

		inline fun border(body: Border.Builder.() -> Unit) = apply {
			border = Border.Builder().apply(body).build()
		}

		inline fun placeholder(body: Placeholder.Builder.() -> Unit) = apply {
			placeholder = Placeholder.Builder().apply(body).build()
		}

		fun build() = AvatarDrawable(this)
	}

	@AvatarDrawableDsl
	class Border(
			@Dimension(unit = Dimension.PX) val width: Int = 0,
			@ColorInt val color: Int = Color.parseColor("#4D8989A8"),
			@ColorInt val colorSecondary: Int = color,
			val gradientAngle: Int = 0,
			val archesCount: Int = 0,
			val archesDegreeArea: Int = 0,
			val archesAngle: Int = 0,
	) {
		@AvatarDrawableDsl
		class Builder {
			@Dimension(unit = Dimension.PX)
			var width: Int = 0
				private set

			@ColorInt
			var color: Int = Color.parseColor("#4D8989A8")
				private set

			@ColorInt
			var colorSecondary: Int? = null
				private set

			var gradientAngle: Int = 0
				private set

			var archesCount: Int = 0
				private set

			var archesDegreeArea: Int = 0
				private set

			var archesAngle: Int = 0
				private set

			fun color(@ColorInt color: Int?) = apply { this.color = color ?: this.color }

			fun colorSecondary(@ColorInt color: Int?) = apply {
				this.colorSecondary = color ?: this.color
			}

			fun archesCount(count: Int) = apply {
				this.archesCount = count
			}

			fun archesDegreeArea(@IntRange(from = 0, to = 360) angle: Int) = apply {
				this.archesDegreeArea = angle
			}

			fun archesAngle(@IntRange(from = 0, to = 360) angle: Int) = apply {
				this.archesAngle = angle
			}

			fun width(@Dimension(unit = Dimension.PX) width: Int?) = apply {
				this.width = width ?: 0
			}

			fun gradientAngle(@IntRange(from = 0, to = 360) angle: Int) = apply {
				this.gradientAngle = angle
			}

			fun build() = Border(
					width = width,
					color = color,
					colorSecondary = colorSecondary ?: color,
					gradientAngle = gradientAngle,
					archesCount = archesCount,
					archesDegreeArea = archesDegreeArea,
					archesAngle = archesAngle
			)
		}
	}

	@AvatarDrawableDsl
	class Placeholder(
			var text: CharSequence? = "?",
			@ColorInt
			var textColor: Int = Color.WHITE,
			@Dimension(unit = Dimension.PX)
			var textSize: Float = 0f,
			var typeface: Typeface? = null,
	) {
		@AvatarDrawableDsl
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

			fun text(text: CharSequence?) = apply { this.text = text ?: "?" }

			fun color(@ColorInt color: Int?) = apply { this.color = color ?: this.color }

			fun size(@Dimension(unit = Dimension.PX) size: Float?) = apply {
				this.size = size ?: 0f
			}

			fun typeface(typeface: Typeface?) = apply { this.typeface = typeface }

			fun build() = Placeholder(text, color, size, typeface)
		}
	}

	enum class Volumetric(val value: Int) {
		NONE(-1),
		ALL(0),
		DRAWABLE(1),
		PLACEHOLDER(2);

		companion object {
			fun from(value: Int) = values().firstOrNull { it.value == value } ?: NONE
		}
	}
}