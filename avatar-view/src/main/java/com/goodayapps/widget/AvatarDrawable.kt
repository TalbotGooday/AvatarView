package com.goodayapps.widget

import android.graphics.*
import android.graphics.drawable.Animatable
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
    private var iconColorFilter: ColorFilter? = null
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
        this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
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

    private val totalArchesDegreeArea
        get() = border.archesDegreeArea.toFloat()

    private val animationLoopDegrees
        get() = border.archesAngle.toFloat()

    private var animationArchesSparseness = 1f

    private val individualArcDegreeLength
        get() = calculateArcDegreeLength()

    private val spaceBetweenArches
        get() = calculateSpaceBetweenArches()

    private val currentAnimationArchesArea
        get() = animationArchesSparseness * totalArchesDegreeArea

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

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())

        var isIconDrawable = false

        val avatarBitmap: Bitmap? = when (avatarDrawable) {
            is BitmapDrawable -> {
                isIconDrawable = false
                val bitmapSize = size - avatarMargin

                ThumbnailUtils.extractThumbnail(avatarDrawable.bitmap.copy(Bitmap.Config.ARGB_8888, false), bitmapSize, bitmapSize)
            }
            is Animatable -> {
                isIconDrawable = false

                createAnimatableAvatarBitmap(avatarDrawable)
            }
            is Drawable -> {
                isIconDrawable = true

                createAvatarBitmap(avatarDrawable, iconDrawableScale)
            }
            else -> {
                null
            }
        }

        //Drawing a background circle and a clip circle for the avatar
        bufferCanvas.drawCircle(
            circleCenter,
            circleCenter,
            backgroundCircleRadius,
            avatarBackgroundPaint
        )

        bufferCanvas.save()
        val path = Path()
        path.addCircle(
            circleCenter,
            circleCenter,
            backgroundCircleRadius,
            Path.Direction.CCW
        )
        bufferCanvas.clipPath(path)

        if (avatarBitmap == null) {
            drawPlaceholder()
        } else {
            drawBitmap(isIconDrawable, avatarBitmap)
        }
        bufferCanvas.restore()

        //Draw volumetric gradient
        drawVolume(avatarBitmap != null)

        //Draw Border
        drawBorder()

        canvas.drawBitmap(bufferBitmap, Matrix(), null)
        canvas.restore()
    }

    private fun createAvatarBitmap(
        avatarDrawable: Drawable,
        scale: Float
    ): Bitmap? {
        val sizeHelper = Size(
            intrinsicWidth = avatarDrawable.intrinsicWidth,
            intrinsicHeight = avatarDrawable.intrinsicHeight,
            max = size,
            scale = scale
        )

        val width = sizeHelper.width
        val height = sizeHelper.height

        return if (width > 0 && height > 0) {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                avatarDrawable.setBounds(0, 0, width, height)

                iconColorFilter?.let { filter -> avatarDrawable.colorFilter = filter }

                avatarDrawable.draw(Canvas(it))
            }
        } else {
            null
        }
    }

    private fun createAnimatableAvatarBitmap(
        avatarDrawable: Drawable
    ): Bitmap? {
        val cropDrawable = CenterCropDrawable(avatarDrawable)

        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
            cropDrawable.setBounds(0, 0, size, size)

            iconColorFilter?.let { filter -> cropDrawable.colorFilter = filter }

            cropDrawable.draw(Canvas(it))
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

    private fun drawBorder() {
        if (border.width > 0) {
            if (((border.archesType == Border.ARCH_TYPE_DEFAULT && border.archesCount > 1)
                        || border.archesType == Border.ARCH_TYPE_MIRROR && border.archesCount > 0)
                && totalArchesDegreeArea > 0f
            ) {
                drawArcBorder()
            } else {
                drawCircleBorder()
            }
        }
    }

    private fun drawArcBorder() {
        val path = Path()

        var totalDegrees = ((270f + animationLoopDegrees) % 360)
        val startSpace =
            if (totalArchesDegreeArea == 360f || border.archesType == Border.ARCH_TYPE_MIRROR) 0f else individualArcDegreeLength
        drawArches(path, totalDegrees + startSpace)

        if (border.archesType == Border.ARCH_TYPE_MIRROR) {
            totalDegrees = ((270f + (animationLoopDegrees + 180)) % 360)
            drawArches(path, totalDegrees + startSpace)
        } else {
            val startOfMainArch = totalDegrees + currentAnimationArchesArea
            path.addArc(arcBorderRect, startOfMainArch, 360f - currentAnimationArchesArea)
        }

        bufferCanvas.drawPath(path, borderPaint)
    }

    private fun drawCircleBorder() {
        bufferCanvas.drawCircle(
            circleCenter,
            circleCenter,
            circleRadius - (border.width / 2f),
            borderPaint
        )
    }

    private fun drawArches(path: Path, totalDegrees: Float) {
        for (i in 0 until border.archesCount) {
            val arcDeg = (individualArcDegreeLength + spaceBetweenArches) * i
            val deg = totalDegrees + arcDeg// * animationArchesSparseness
            path.addArc(arcBorderRect, deg, individualArcDegreeLength)
        }
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

    private fun calculateArcDegreeLength() =
        totalArchesDegreeArea / (if (border.archesType == Border.ARCH_TYPE_DEFAULT) {
            (border.archesCount * 2) + 1
        } else {
            border.archesCount
        })

    class Size(intrinsicWidth: Int, intrinsicHeight: Int, max: Int, private val scale: Float) {
        private val _width: Float
        private val _height: Float

        val width: Int
            get() = (_width * scale).toInt()
        val height: Int
            get() = (_height * scale).toInt()

        init {
            val maxF = max.toFloat()

            val intrinsicHeightF = intrinsicHeight.toFloat()
            val intrinsicWidthF = intrinsicWidth.toFloat()

            val ratioOfWidth = intrinsicWidthF / maxF
            val ratioOfHeight = intrinsicHeightF / maxF
            val ratio = intrinsicWidth / intrinsicHeightF

            when {
                ratio > 1f -> {
                    _width = maxF
                    _height = intrinsicHeight / ratioOfHeight / ratio
                }
                ratio < 1f -> {
                    _width = intrinsicWidth / ratioOfWidth * ratio
                    _height = maxF
                }
                ratio == 1f -> {
                    _width = maxF
                    _height = maxF
                }
                else -> {
                    _width = 0f
                    _height = 0f
                }
            }
        }
    }

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

        fun iconDrawableScale(@FloatRange(from = 0.0) scale: Float) =
            apply { this.iconDrawableScale = scale }

        fun size(@Dimension(unit = Dimension.PX) size: Int) = apply { this.size = size }

        fun avatarMargin(@Dimension(unit = Dimension.PX) margin: Int) =
            apply { this.avatarMargin = margin }

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
        val archesType: Int = ARCH_TYPE_DEFAULT,
    ) {
        companion object {
            const val ARCH_TYPE_DEFAULT = 0
            const val ARCH_TYPE_MIRROR = 1
        }

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

            var archesType: Int = 0
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

            fun archesType(@IntRange(from = 0, to = 2) type: Int) = apply {
                this.archesType = type
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
                archesAngle = archesAngle,
                archesType = archesType,
            )
        }
    }

    @AvatarDrawableDsl
    class Placeholder(
        var text: CharSequence? = "?",
        @ColorInt
        var textColor: Int = Color.WHITE,
        @Dimension(unit = Dimension.PX)
        var textSize: Float = 30f,
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
            var size: Float = 30f
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