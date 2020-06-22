package com.ccc.verificationcodeview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ccc.vcv.VerificationCodeView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), VerificationCodeView.OnInputVerificationCodeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verificationView.setOnInputVerificationCodeListener(this)

        styleUnderLine.autoLinkMask

        styleBox.setOnClickListener {
            verificationView.style = VerificationCodeView.Style.BOX
            verificationView.refresh()
        }

        styleUnderLine.setOnClickListener {
            verificationView.style = VerificationCodeView.Style.UNDERLINE
            verificationView.refresh()
        }
    }

    override fun onInputVerificationCodeComplete() {
        editText.requestFocus()
    }
}
