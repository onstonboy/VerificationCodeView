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
    }

    override fun onInputVerificationCodeComplete() {
        editText.requestFocus()
    }
}
