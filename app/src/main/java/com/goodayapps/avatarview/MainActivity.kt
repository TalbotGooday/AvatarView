package com.goodayapps.avatarview

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import coil.Coil
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.goodayapps.avatarview.databinding.ActivityMainBinding
import com.goodayapps.widget.AvatarDrawable
import com.goodayapps.widget.AvatarView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        binding.avatar111.load("https://media4.giphy.com/media/f8hd7QP9LT31Rk2NG1/giphy.gif")
    }

    private fun initViews() {
        binding.iconDrawableScaleSeek.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val processFl = progress / 100f

                applyToAvatars {
                    it.iconDrawableScale = processFl
                }
            }
        })
        binding.sizeSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
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
        binding.avBorderWidth.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.borderWidth = convertDpToPixel(progress)
                }
            }
        })
        binding.avTextSizePercentage.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val processFl = progress / 100f

                applyToAvatars {
                    it.textSizePercentage = processFl
                }
            }
        })
        binding.avAvatarMargin.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.avatarMargin = convertDpToPixel(progress)
                }
            }
        })

        binding.avVolumetric.setOnCheckedChangeListener { _, checkedId ->
            applyToAvatars {
                it.volumetricType = when (checkedId) {
                    R.id.avVolumetricAll -> AvatarDrawable.Volumetric.ALL
                    R.id.avVolumetricDrawable -> AvatarDrawable.Volumetric.DRAWABLE
                    R.id.avVolumetricPlaceholder -> AvatarDrawable.Volumetric.PLACEHOLDER
                    else -> AvatarDrawable.Volumetric.NONE
                }
            }
        }
        binding.avBorderGradientAngle.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.borderGradientAngle = progress
                }
            }
        })

        binding.archesDegreeArea.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.archesDegreeArea = progress
                }
            }
        })

        binding.archesCount.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.archesCount = progress
                }
            }
        })
        binding.archesAngle.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyToAvatars {
                    it.archesAngle = progress
                }
            }
        })
    }

    private inline fun applyToAvatars(action: (AvatarView) -> Unit) {
        binding.avatarsList.children.mapNotNull { it as? AvatarView }.forEach { action.invoke(it) }
    }

    private fun convertDpToPixel(dp: Int): Int {
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }
}