package com.goodayapps.widget

inline fun avatarDrawable(block: AvatarDrawable.Builder.() -> Unit): AvatarDrawable = AvatarDrawable.Builder().apply(block).build()

@DslMarker
annotation class AvatarDrawableDsl

@DslMarker
annotation class TextOverDrawableDsl
