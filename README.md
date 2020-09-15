# AvatarView

[![](https://jitpack.io/v/TalbotGooday/AvatarView.svg)](https://jitpack.io/#TalbotGooday/AvatarView)
[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)]()

A simple AvatarView based on the AppCompatImageView

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
## Code example

Settle the AvatarView somewhere in your XML like this:

```xml
<com.goodayapps.widget.AvatarView
	android:id="@+id/avatarView1"
	android:layout_width="@dimen/avatar_size"
	android:layout_height="@dimen/avatar_size"
	android:src="@drawable/ic_launcher_foreground"
	app:avBackgroundColor="@color/colorPrimary"
	app:avBorderColor="@color/colorAccent"
	app:avBorderWidth="5dp"
	app:avVolumetricDrawable="false"
	app:avVolumetricPlaceholder="false"
	app:iconDrawableScale=".7"
	app:placeholderText="AV" />
```

Add the dependency:
```Groovy
dependencies {
      implementation 'com.github.TalbotGooday:AvatarView:x.x.x'
}
```


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details