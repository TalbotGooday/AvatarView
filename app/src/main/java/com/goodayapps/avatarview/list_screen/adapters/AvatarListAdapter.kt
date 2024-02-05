package com.goodayapps.avatarview.list_screen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.goodayapps.avatarview.R
import com.goodayapps.avatarview.databinding.ItemListBinding
import com.goodayapps.avatarview.loadWithBlurHash
import kotlin.random.Random

class AvatarListAdapter : RecyclerView.Adapter<AvatarListAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return 1000
    }

    class Holder(private val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() = with(binding) {
            avatar.isVisible = Random.nextBoolean()

            if (Random.nextBoolean()) {
                avatar.loadWithBlurHash(
                    src = "https://www.terriwindling.com/.a/6a00e54fcf7385883401b7c74c966d970b-800wi",
                    blurHash = "UGAKELD+bxs*_Ko#N0%KxuxtoeR+NNobxrM#"
                )
            } else {
                avatar.load("https://media4.giphy.com/media/f8hd7QP9LT31Rk2NG1/giphy.gif")
            }
            avatar.backgroundPlaceholderColor =
                ResourcesCompat.getColor(root.resources, R.color.colorPrimary, null)
        }
    }
}
