package com.yulmaso.maskedet

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
 *
 *  Универсальная вьюха для ввода текста с маской. Для работы необходимо установить настройки
 *  форматирования, вызвав метод [setFormattingSettings].
 *
 *  TODO:
 *      1) протестировать сохранение состояния (onSaveInstanceState, onRestoreInstanceState)
 *      2) реализовать возможность выделения текста
 *      3) реализовать возможность переставлять курсор и осуществлять корректное форматирование
 *      при редактировании в середине строки
 */
class MaskedEt(context: Context, attrs: AttributeSet): AppCompatEditText(context, attrs), TextWatcher {

    companion object {
        /**
         *  Символы, отмеченные в [maskSymbolsMeaning] этим значением, являются служебными и
         *  вписываются в строку автоматически при достижении курсора их позиции.
         *
         *  Пробел всегда является служебным символом.
         */
        const val PATTERN_VALUE = "pattern"
    }

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

    /**
     *  Метод инициализации алгоритма форматирования. Без вызова данного метода вьюха работает как
     *  простой EditText.
     *
     *  @param mask                 - Маска, по которой осуществляется форматирование.
     *  @param maskSymbolsMeaning   - Описание каждого символа, из которых состоит [mask].
     *  @param showHintOnEdit       - Флажок, по которому определяется необходимость отображения
     *  hint'a во время ввода текста. (Также может быть установлен с помощью атрибута
     *  [R.styleable.MaskedEt_show_hint_on_edit].
     */
    fun setFormattingSettings(
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

    /**
     *  Метод сброса алгоритма форматирования.
     */
    fun resetFormatting() {
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

    /**
     *  Метод получения "чистого" текста (текста только из тех символов, которые ввел
     *  пользователь сам, без служебных символов).
     */
    fun getRawText(): String {
        return if (initialized) getRawText(text.toString()) else text.toString()
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

        val sb = SpannableStringBuilder()
        val raw = if (text.length == 1) text else getRawText(text.toString())
        lastValidPosition = -1

        if (raw.isEmpty()) {
            return ""
        }

        fun setFurtherAsHint(mIndex: Int): CharSequence {
            sb.append(m.subSequence(mIndex, m.length))
            sb.setSpan(ForegroundColorSpan(currentHintTextColor), mIndex, sb.length, 0)
            return sb
        }

        var lastRawIndex = -1

        rm.forEachIndexed lit@{ mIndex, rawIndex ->
            if (rawIndex != -1) lastRawIndex = rawIndex

            if (rawIndex < raw.length) {
                if (rawIndex == -1) {
                    if (removeAction && lastRawIndex == raw.lastIndex) {
                        return setFurtherAsHint(mIndex)
                    } else {
                        sb.append(m[mIndex])
                        lastValidPosition++
                        return@lit
                    }
                }

                if (msm[m[mIndex]]?.contains(raw[rawIndex]) == true) {
                    sb.append(raw[rawIndex])
                    lastValidPosition++
                } else {
                    return setFurtherAsHint(mIndex)
                }

            } else if (showHint) {
                return setFurtherAsHint(mIndex)
            } else {
                return sb.substring(0, rawIndex)
            }
        }

        return sb
    }

    private fun getRawText(text: String): String {
        val rm = checkNotNull(resolvedMask)
        val sb = StringBuilder()

        rm.forEachIndexed { mIndex, rawIndex ->
            if (rawIndex != -1 && mIndex < lastTypedPosition) {
                sb.append(text[mIndex])
            }
        }
        return sb.toString()
    }
}