package com.example.textmasktest

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

/**
 *  Created by yulmaso
 *  Date: 21.07.2021
 */
class MaskedEt(context: Context, attrs: AttributeSet): AppCompatEditText(context, attrs), TextWatcher {

    private var showHint: Boolean

    private var mask: String? = null
    private var maskSymbolsMeaning: Map<Char, String>? = null
    private var resolvedMask: Array<Int>? = null

    private var initialized: Boolean = false
    private var lastTypedPosition: Int = -1
    private var lastValidPosition: Int = -1

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MaskedEt, 0, 0).apply {
            showHint = getBoolean(R.styleable.MaskedEt_show_hint_on_edit, false)
        }
        addTextChangedListener(this)
    }

    companion object {
        const val PATTERN_VALUE = "pattern"
    }

    /**
     *  Метод инициализации алгоритма форматирования. Без вызова данного метода вьюха работает как
     *  простой EditText.
     *
     *  @param mask                 - Маска, по которой осуществляется форматирование. (Пробел -
     *  всегда по дефолту PATTERN_VALUE).
     *  @param maskSymbolsMeaning   - Описание каждого символа, из которых состоит [mask].
     *  @param showHintOnEdit       - Флажок, по которому определяется необходимость отображения
     *  hint'a после введенного текста.
     */
    fun initFormattingSettings(
        mask: String,
        maskSymbolsMeaning: Map<Char, String>,
        showHintOnEdit: Boolean = false
    ) {
        val msm = maskSymbolsMeaning.toMutableMap().apply { put(' ', PATTERN_VALUE) }
        this.hint = mask
        this.mask = mask
        this.maskSymbolsMeaning = msm
        this.showHint = showHintOnEdit

        val result = mutableListOf<Int>()
        var index = 0
        mask.forEach { c ->
            msm[c]?.let { value ->
                if (value == PATTERN_VALUE) {
                    result.add(-1)
                } else {
                    result.add(index)
                    index++
                }
            } ?: throw IllegalStateException("Char '$c' is not explained for MaskedEt")
        }

        resolvedMask = result.toTypedArray()
        initialized = false
        setText("")
        initialized = true
    }

    fun reset() {
        hint = null
        mask = null
        maskSymbolsMeaning = null
        resolvedMask = null
        showHint = false
        lastTypedPosition = -1
        lastValidPosition = -1
        initialized = false
        setText("")
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = Bundle()
        state.putParcelable("super", super.onSaveInstanceState())
        state.putBoolean("showHint", showHint)
        state.putString("mask", mask)
        maskSymbolsMeaning?.let {
            val hashMap = HashMap<Char, String>().apply { putAll(it) }
            state.putSerializable("maskSymbolsMeaning", hashMap)
        }
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        showHint = bundle.getBoolean("showHint", false)
        mask = bundle.getString("mask", null)
        bundle.getSerializable("maskSymbolsMeaning")?.let {
            maskSymbolsMeaning = (it as HashMap<Char, String>).toMap()
        }
        super.onRestoreInstanceState(bundle.getParcelable("super"))
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (initialized) {
            setSelection(lastValidPosition + 1)
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    var removeAction = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        if (initialized && lengthBefore > lengthAfter) {
            removeAction = true
            lastTypedPosition -= lengthBefore - lengthAfter
        }
    }
    override fun afterTextChanged(s: Editable?) {
        if (initialized) {
            removeTextChangedListener(this)
            s?.let {
                lastTypedPosition = selectionEnd
                val formatted = formatText(it)
                setText(formatted)
                setSelection(lastValidPosition + 1)
                removeAction = false
            }
            addTextChangedListener(this)
        }
    }

    private fun formatText(text: CharSequence): CharSequence {
        val m = checkNotNull(mask)
        val msm = checkNotNull(maskSymbolsMeaning)
        val rm = checkNotNull(resolvedMask)

        val sb = SpannableStringBuilder(m)
        val raw = getRawText(text.toString())

        if (raw.isEmpty()) {
            lastValidPosition = -1
            return ""
        }

        var end = false
        rm.forEachIndexed lit@{ mIndex, rawIndex ->
            if (mIndex > lastTypedPosition) end = true

            if (rawIndex > raw.lastIndex && !showHint && !end)
                return sb.substring(0, lastValidPosition + 1)

            if (rawIndex == -1 && lastValidPosition == mIndex - 1 && !removeAction && !end) {
                lastValidPosition++
                return@lit
            }

            if (rawIndex > -1 && rawIndex < raw.length && msm[m[mIndex]]?.contains(raw[rawIndex]) == true && !end) {
                sb.replace(mIndex, mIndex + 1, raw[rawIndex].toString())
                lastValidPosition = mIndex
                return@lit
            }

            sb.setSpan(ForegroundColorSpan(currentHintTextColor), mIndex, mIndex + 1, 0)
        }
        return sb
    }

    private fun getRawText(text: String): String {
        val rm = checkNotNull(resolvedMask)
        val sb = StringBuilder()
        text.forEachIndexed { index, c ->
            if (index < rm.size && rm[index] != -1 && index < lastTypedPosition) sb.append(c)
        }
        return sb.toString()
    }

    fun getRawText(): String {
        return if (initialized) getRawText(text.toString()) else text.toString()
    }
}