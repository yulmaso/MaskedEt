package com.example.textmasktest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val TRANSPORT_NUM_LETTERS = "ABEKMHOPCTYXDАВЕКМНОРСТУХ"
        const val TRANSPORT_NUM_DIGITS = "0123456789"
    }

    private val et by lazy { findViewById<MaskedEt>(R.id.et) }
    private val tv by lazy { findViewById<TextView>(R.id.tv) }
    private val btn by lazy { findViewById<Button>(R.id.btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        et.initFormattingSettings(
            "+7 (900) 000-00-00",
            mapOf(
                '0' to TRANSPORT_NUM_DIGITS,
                '+' to MaskedEt.PATTERN_VALUE,
                '7' to MaskedEt.PATTERN_VALUE,
                '(' to MaskedEt.PATTERN_VALUE,
                ')' to MaskedEt.PATTERN_VALUE,
                '9' to MaskedEt.PATTERN_VALUE,
                '-' to MaskedEt.PATTERN_VALUE
            ),
            true
        )

        btn.setOnClickListener {
            tv.text = et.getRawText()
        }
    }
}