package com.yulmaso.maskedEtSample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yulmaso.maskedet.MaskedEt

class MainActivity : AppCompatActivity() {

    companion object {
        const val ENABLED_DIGITS = "0123456789"
    }

    private val et by lazy { findViewById<MaskedEt>(R.id.et) }
    private val tv by lazy { findViewById<TextView>(R.id.tv) }
    private val rawTextBtn by lazy { findViewById<Button>(R.id.raw_text_btn) }
    private val initBtn by lazy { findViewById<Button>(R.id.init_btn) }
    private val resetBtn by lazy { findViewById<Button>(R.id.reset_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initEt()

        rawTextBtn.setOnClickListener {
            tv.text = et.getRawText()
        }

        initBtn.setOnClickListener {
            initEt()
        }

        resetBtn.setOnClickListener {
            et.resetFormatting()
        }
    }

    private fun initEt() {
        et.setFormattingSettings(
            "+7 (000) 000-00-00",
            mapOf(
                '0' to ENABLED_DIGITS,
                '+' to MaskedEt.PATTERN_VALUE,
                '7' to MaskedEt.PATTERN_VALUE,
                '(' to MaskedEt.PATTERN_VALUE,
                ')' to MaskedEt.PATTERN_VALUE,
                '-' to MaskedEt.PATTERN_VALUE
            ),
            true
        )
    }
}