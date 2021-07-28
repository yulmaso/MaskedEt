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
 *  View for entering text with a mask. To work, you need to set the formatting settings by calling
 *  the method [setFormattingSettings].
 *
 *  TODO:
 *      1) implement the ability to select text
 *      2) implement the ability to move cursor and perform correct formatting while user edits
 *      text the middle of line
 */
class MaskedEt(
    context: Context,
    private val attrs: AttributeSet
) : AppCompatEditText(context, attrs), TextWatcher {

    companion object {
        /**
         *  Mask chars marked with this value in [maskSymbolsMeaning] become service symbols and
         *  the formatting algorithm inserts them into line automatically when the cursor reaches
         *  their positions. (Space in mask is always a service symbol)
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
    private var removeAction = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MaskedEt, 0, 0).apply {
            showHint = getBoolean(R.styleable.MaskedEt_show_hint_on_edit, false)
        }
        addTextChangedListener(this)
    }

    /**
     *  Formatting algorithm initialization method. Without calling that view will work as a common
     *  EditText.
     *
     *  @param mask                 - Mask for formatting.
     *  @param maskSymbolsMeaning   - Description of every [mask] symbol.
     *  @param showHintOnEdit       - Flag, that handles wether to show mask symbols while editing
     *  in front of cursor, or show it only as a hint. (Also can be set with an attribute
     *  [R.styleable.MaskedEt_show_hint_on_edit])
     */
    fun setFormattingSettings(
        mask: String,
        maskSymbolsMeaning: Map<Char, String>,
        showHintOnEdit: Boolean? = null
    ) {
        val msm = maskSymbolsMeaning.toMutableMap().apply { put(' ', PATTERN_VALUE) }
        this.hint = mask
        this.mask = mask
        this.maskSymbolsMeaning = msm
        showHintOnEdit?.let { showHint = it }

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
            } ?: throw IllegalStateException("Char '$c' in mask is not explained for MaskedEt")
        }

        resolvedMask = result.toTypedArray()
        initialized = false
        setText("")
        initialized = true
    }

    /**
     *  Method for resetting formatting algorithm.
     */
    fun resetFormatting() {
        initialized = false
        hint = null
        mask = null
        maskSymbolsMeaning = null
        resolvedMask = null
        showHint = context.theme
            .obtainStyledAttributes(attrs, R.styleable.MaskedEt, 0, 0)
            .getBoolean(R.styleable.MaskedEt_show_hint_on_edit, false)

        lastTypedPosition = -1
        lastValidPosition = -1
        removeAction = false
        setText("")
    }

    /**
     *  Method for getting raw text.
     *
     *  @return String without service symbols
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
        resolvedMask?.let {
            state.putIntArray("resolvedMask", it.toIntArray())
        }
        state.putBoolean("initialized", initialized)
        state.putInt("lastTypedPosition", lastTypedPosition)
        state.putInt("lastValidPosition", lastValidPosition)
        state.putBoolean("removeAction", removeAction)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        showHint = bundle.getBoolean("showHint", false)
        mask = bundle.getString("mask", null)
        bundle.getSerializable("maskSymbolsMeaning")?.let {
            maskSymbolsMeaning = (it as HashMap<Char, String>).toMap()
        }
        bundle.getIntArray("resolvedMask")?.let {
            resolvedMask = it.toTypedArray()
        }
        initialized = state.getBoolean("initialized")
        lastTypedPosition = state.getInt("lastTypedPosition")
        lastValidPosition = state.getInt("lastValidPosition")
        removeAction = state.getBoolean("removeAction")
        super.onRestoreInstanceState(bundle.getParcelable("super"))
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (initialized) {
            setSelection(lastValidPosition + 1)
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    private var onTextChanged = false
    private var onAfterChanged = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        if (!onTextChanged) {
            onTextChanged = true
            if (initialized && lengthBefore > lengthAfter) {
                removeAction = true
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (onTextChanged && !onAfterChanged) {
            onAfterChanged = true

            if (initialized) {
                removeTextChangedListener(this)
                s?.let {
                    lastTypedPosition = selectionEnd
                    setText(formatText(it))
                    setSelection(lastValidPosition + 1)
                    removeAction = false
                }
                addTextChangedListener(this)
            }

            onTextChanged = false
            onAfterChanged = false
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

        var lastHandledRawIndex = -1

        rm.forEachIndexed lit@{ mIndex, rawIndex ->
            if (rawIndex != -1) lastHandledRawIndex = rawIndex

            if (rawIndex < raw.length) {
                if (rawIndex == -1) {
                    if (removeAction && lastHandledRawIndex == raw.lastIndex) {
                        return setFurtherAsHint(mIndex)
                    }

                    sb.append(m[mIndex])
                    lastValidPosition++
                    return@lit
                }

                if (msm[m[mIndex]]?.contains(raw[rawIndex]) == true) {
                    sb.append(raw[rawIndex])
                    lastValidPosition++
                    return@lit
                }

                return setFurtherAsHint(mIndex)
            }

            return if (showHint) {
                setFurtherAsHint(mIndex)
            } else {
                sb.substring(0, rawIndex)
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