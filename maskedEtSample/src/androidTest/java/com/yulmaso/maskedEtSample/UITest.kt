package com.yulmaso.maskedEtSample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
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

    @Test
    fun testCorrectInput() {
        val et = onView(withId(R.id.et))

        et.perform(click())
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)
        et.perform(typeText("1"))
        Thread.sleep(DELAY)

        onView(withId(R.id.et)).check(matches(withText("+7 (111) 111-11-11")))
    }
}