package com.goodayapps.avatarview

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import com.goodayapps.widget.AvatarDrawable
import com.goodayapps.widget.AvatarView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
		initCoil(this)

        initViews()

//        avatar111.load("https://media4.giphy.com/media/f8hd7QP9LT31Rk2NG1/giphy.gif")
        avatar111.load("https://comunitee.b-cdn.net/staging/avatars/users/hlHkulpffffqQr3NWcVKxI6GbSB3/65E808E7-3098-45CE-B0D6-E50B12912751_1640027738.jpeg")
    }

    private fun initViews() {
        iconDrawableScaleSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val processFl = progress / 100f

                applyToAvatars {
                    it.iconDrawableScale = processFl
                }
            }
        })
        sizeSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val processFl = progress / 100f
                val newSize = (convertDpToPixel(100) * processFl).toInt()

                applyToAvatars {
                    it.layoutParams = it.layoutParams.apply {
                        width = newSize
                        height = newSize
                    }
                }
            }
        })
        avBorderWidth.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.borderWidth = convertDpToPixel(progress)
                }
            }
        })
        avTextSizePercentage.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val processFl = progress / 100f

                applyToAvatars {
                    it.textSizePercentage = processFl
                }
            }
        })
        avAvatarMargin.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.avatarMargin = convertDpToPixel(progress)
                }
            }
        })

        avVolumetric.setOnCheckedChangeListener { _, checkedId ->
            applyToAvatars {
                it.volumetricType = when (checkedId) {
                    R.id.avVolumetricAll -> AvatarDrawable.Volumetric.ALL
                    R.id.avVolumetricDrawable -> AvatarDrawable.Volumetric.DRAWABLE
                    R.id.avVolumetricPlaceholder -> AvatarDrawable.Volumetric.PLACEHOLDER
                    else -> AvatarDrawable.Volumetric.NONE
                }
            }
        }
        avBorderGradientAngle.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.borderGradientAngle = progress
                }
            }
        })

        archesDegreeArea.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.archesDegreeArea = progress
                }
            }
        })

        archesCount.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.archesCount = progress
                }
            }
        })
        archesAngle.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.archesAngle = progress
                }
            }
        })
    }

    private inline fun applyToAvatars(action: (AvatarView) -> Unit) {
        avatars_list.children.mapNotNull { it as? AvatarView }.forEach { action.invoke(it) }
    }

    private fun convertDpToPixel(dp: Int): Int {
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun initCoil(context: Context): ImageLoader {
        val builder = ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .componentRegistry {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder(context))
                } else {
                    add(GifDecoder())
                }
                add(SvgDecoder(context))
            }

        val imageLoader = builder.build()

        Coil.setImageLoader(imageLoader)

        return imageLoader
    }

}