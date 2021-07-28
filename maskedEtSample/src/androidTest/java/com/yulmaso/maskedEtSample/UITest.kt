package com.yulmaso.maskedEtSample

import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by yulmaso
 * Date: 27.07.2021
 */
class UITest {

    companion object {
        const val DELAY = 100L
    }

    @get:Rule
    val mActivityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        onView(withId(R.id.init_btn)).perform(click())
    }

    @Test
    fun testCorrectInput1() {
        val et = onView(withId(R.id.et))

        et.perform(click(), typeText("1111111111"))
        Thread.sleep(DELAY)

        et.check(matches(withText("+7 (111) 111-11-11")))

        onView(withId(R.id.raw_text_btn)).perform(click())
        Thread.sleep(DELAY)
        onView(withId(R.id.tv)).check(matches(withText("1111111111")))
    }

    @Test
    fun testCorrectInput5() {
        val et = onView(withId(R.id.et))

        et.perform(click(), typeText("5555555555"))
        Thread.sleep(DELAY)

        et.check(matches(withText("+7 (555) 555-55-55")))

        onView(withId(R.id.raw_text_btn)).perform(click())
        Thread.sleep(DELAY)
        onView(withId(R.id.tv)).check(matches(withText("5555555555")))
    }

    @Test
    fun testInputAndDelete() {
        val et = onView(withId(R.id.et))

        et.perform(
            click(),
            typeText("55555"),
            pressKey(KeyEvent.KEYCODE_DEL),
            pressKey(KeyEvent.KEYCODE_DEL),
            pressKey(KeyEvent.KEYCODE_DEL),
            typeText("55778890")
        )
        Thread.sleep(DELAY)

        et.check(matches(withText("+7 (555) 577-88-90")))

        onView(withId(R.id.raw_text_btn)).perform(click())
        Thread.sleep(DELAY)
        onView(withId(R.id.tv)).check(matches(withText("5555778890")))
    }
}