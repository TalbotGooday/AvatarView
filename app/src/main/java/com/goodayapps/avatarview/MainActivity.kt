package com.goodayapps.avatarview

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.goodayapps.widget.AvatarDrawable
import com.goodayapps.widget.AvatarView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		initViews()
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
					R.id.avVolumetricAll -> AvatarDrawable.VolumetricType.ALL
					R.id.avVolumetricDrawable -> AvatarDrawable.VolumetricType.DRAWABLE
					R.id.avVolumetricPlaceholder -> AvatarDrawable.VolumetricType.PLACEHOLDER
					else -> AvatarDrawable.VolumetricType.NONE
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

		avAngle.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				applyToAvatars {
					it.labelTextAngle = progress
				}
			}
		})

		avLabelBgWidth.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				applyToAvatars {
					it.labelBackgroundWidth = convertDpToPixel(progress)
				}
			}
		})


	}

	private inline fun applyToAvatars(action: (AvatarView) -> Unit) {
		arrayOf(avatarView0, avatarView1, avatarView2, avatarView3).forEach { action.invoke(it) }
	}

	private fun convertDpToPixel(dp: Int): Int {
		val metrics = resources.displayMetrics
		return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
	}

}