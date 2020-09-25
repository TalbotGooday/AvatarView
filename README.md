# AvatarView

[![](https://jitpack.io/v/TalbotGooday/AvatarView.svg)](https://jitpack.io/#TalbotGooday/AvatarView)
[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)]()

A simple AvatarView based on the AppCompatImageView.
Contain

<img src="/screenshots/1.png" width=32%/>

## Getting started

Add to your root build.gradle:
```Groovy
allprojects {
	repositories {
	    ...
	    maven { url "https://jitpack.io" }
	}
}
```

Add the dependency:
```Groovy
dependencies {
      implementation 'com.github.TalbotGooday:AvatarView:x.x.x'
}
```

## Code example

Settle the AvatarView somewhere in your XML like this:

```xml
<com.goodayapps.widget.AvatarView
	android:id="@+id/avatarView"
	android:layout_width="@dimen/avatar_size"
	android:layout_height="@dimen/avatar_size"
	android:src="drawable"
	app:avBackgroundColor="color|reference"
	app:avBorderColor="color|reference"
	app:avBorderColorSecondary="color|reference"
	app:avBorderWidth="dimension"
	app:avVolumetricType="none|all|drawable|placeholder"
	app:avAvatarMargin="dimension"
	app:avTextSizePercentage="float"
	app:iconDrawableScale="float"
	app:placeholderText="string" />
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
