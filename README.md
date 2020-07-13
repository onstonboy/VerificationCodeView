# VerificationCodeView

[![](https://jitpack.io/v/onstonboy/VerificationCodeView.svg)](https://jitpack.io/#onstonboy/VerificationCodeView)

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.onstonboy:VerificationCodeView:Tag'
	}

Demo:

[![Screenshot from Gyazo](https://gyazo.com/85fa6771c8740ef78bb027911f0584ab/raw)](https://gyazo.com/85fa6771c8740ef78bb027911f0584ab)

Attributes:

| Name | Format | Default value |
| --- | --- | --- |
| `vcv_spacingItem` | dimension | `20dp` |
| `vcv_inputWidth` | dimension | `30dp` |
| `vcv_inputHeight` | dimension | `56dp` |
| `vcv_radius` | dimension | `5dp` |
| `vcv_inputCount` | dimension | `6` |
| `vcv_lineColor` | dimension | `#ff000000` |
| `vcv_lineActiveColor` | dimension | `#2D7FF9` |
| `vcv_textColor` | dimension | `#ff000000` |
| `vcv_textSize` | dimension | `18sp` |
| `vcv_style` | dimension | `UNDERLINE` or `BOX` |

How to use:

- `Xml`
``` xml
 <com.ccc.vcv.VerificationCodeView
        android:id="@+id/verificationView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:vcv_cursorColor="#B7BCD6"
        app:vcv_inputHeight="@dimen/dp_56"
        app:vcv_inputWidth="@dimen/dp_50"
        app:vcv_lineColor="#B7BCD6"
        app:vcv_radius="@dimen/dp_8"
        app:vcv_spacingItem="@dimen/dp_10"
        app:vcv_style="underline"
        app:vcv_textColor="@android:color/black"
        app:vcv_textSize="@dimen/sp_24" />
```

- `kotlin`
``` kotlin
 val verificationCode = VerificationCodeView(this).apply {
            setHexColorCursor(Color.parseColor("#B7BCD6"))
            setHexColorLine(Color.parseColor("#B7BCD6"))
            setHexColorText(Color.parseColor("#B7BCD6"))
            setColorLineActive(R.color.colorPrimary)
            setHeightInput(R.dimen.dp_56)
            setWidthInput(R.dimen.dp_50)
            setRadius(R.dimen.dp_8)
            setSpacingItem(R.dimen.dp_10)
            setTextSize(R.dimen.sp_24)
            style = VerificationCodeView.Style.BOX
        }
   verficationCode.refresh()
```

Listener:
to detect input verification code completly, use
``` kotlin
verificationView.setOnInputVerificationCodeListener(this)

override fun onInputVerificationCodeComplete() {
        // ur logic
    }
```
