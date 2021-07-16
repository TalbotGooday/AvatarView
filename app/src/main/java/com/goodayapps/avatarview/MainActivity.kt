package com.goodayapps.avatarview

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.goodayapps.widget.AvatarDrawable
import com.goodayapps.widget.AvatarView
import com.goodayapps.widget.avatarDrawable
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

}