package com.goodayapps.avatarview

import coil.load
import coil.request.ImageRequest
import com.goodayapps.widget.AvatarView

fun AvatarView.loadWithBlurHash(src: String, blurHash: String) {
    this.blurHash = blurHash

    val builder: ImageRequest.Builder.() -> Unit = {
        placeholder(this@loadWithBlurHash.drawable)
        error(this@loadWithBlurHash.drawable)
    }

    load(src, builder = builder)
}