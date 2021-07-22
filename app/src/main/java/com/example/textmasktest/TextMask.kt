package com.example.textmasktest

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

/**
 *  Created by yulmaso
 *  Date: 20.07.2021
 */
class TextMask {

    var mask: String? = null
    var maskSymbolMeaning: Map<Char, String>? = null

    var textColor: Int = Color.BLACK
    var hintColor: Int? = null

    fun getMaskedText(e: String): Spannable {
        // Делаем проверку, что все необходимые поля заполнены
        val m = checkNotNull(mask)
        val map = checkNotNull(maskSymbolMeaning)
        m.toCharArray().distinct().forEach { assert(map.containsKey(it)) }
        val result = StringBuilder()

        // Отсекаем символы в еонце, если длина строки больше маски и проверяем, что
        // строка соответствует маске
        e
            .let {
                if (e.length > m.length) it.substring(0, m.length) else it
            }
            .forEachIndexed { index, c ->
                if (map[m[index]]?.contains(c) == true) result.append(c)
            }

        // Если длина отформатированной строки меньше маски, докидываем в строку маску
        val approvedLength = result.length
        if (approvedLength < m.length && hintColor != null) {
            (approvedLength..m.lastIndex).forEach { result.append(m[it]) }
        }

        // Красим пользовательские символы в [textColor]
        val spannable = SpannableString(result)
        spannable.setSpan(
            ForegroundColorSpan(textColor),
            0,
            approvedLength,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Красим символы маски в [hintColor]
        hintColor?.let { color ->
            if (approvedLength < m.lastIndex) {
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    approvedLength,
                    spannable.length,
                    0
                )
            }
        }

        return spannable
    }

}