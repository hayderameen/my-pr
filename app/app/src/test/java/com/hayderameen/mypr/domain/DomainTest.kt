package com.hayderameen.mypr.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatesTest {
    @Test
    fun `102_5 kg barbell is 25 + 15 + 1_25 per side`() {
        val side = platesPerSide(102.5, WeightUnit.KG).map { it.weight }
        assertEquals(listOf(25.0, 15.0, 1.25), side)
        assertEquals(102.5, 20 + side.sum() * 2, 1e-9)
    }

    @Test
    fun `empty bar has no plates`() {
        assertTrue(platesPerSide(20.0, WeightUnit.KG).isEmpty())
        assertTrue(platesPerSide(45.0, WeightUnit.LBS).isEmpty())
    }

    @Test
    fun `lbs uses pound plates on a 45 lb bar`() {
        assertEquals(listOf(45.0, 45.0), platesPerSide(225.0, WeightUnit.LBS).map { it.weight })
        assertEquals(listOf(45.0, 35.0, 5.0, 2.5), platesPerSide(220.0, WeightUnit.LBS).map { it.weight })
        assertTrue(platesPerSide(315.0, WeightUnit.LBS).all { it.unit == WeightUnit.LBS })
    }

    @Test
    fun `1000 kg barbell loads 19 reds plus change`() {
        val side = platesPerSide(1000.0, WeightUnit.KG)
        assertEquals(1000.0, 20 + side.sumOf { it.weight } * 2, 1e-9)
        assertEquals(19, side.count { it.weight == 25.0 })
    }

    @Test
    fun `bodyweight breakdown uses whole plates`() {
        assertEquals(listOf(20.0, 2.5), breakdown(22.5, WeightUnit.KG).map { it.weight })
        assertTrue(breakdown(0.0, WeightUnit.KG).isEmpty())
    }
}

class WeightsTest {
    @Test
    fun `conversion round trips`() {
        assertEquals(102.0583, toKg(225.0, WeightUnit.LBS), 1e-4)
        assertEquals(225.0, fromKg(toKg(225.0, WeightUnit.LBS), WeightUnit.LBS), 1e-9)
    }

    @Test
    fun `display keeps what was entered in the same unit`() {
        val kg = toKg(225.0, WeightUnit.LBS)
        assertEquals(225.0, displayWeight(kg, 225.0, WeightUnit.LBS, WeightUnit.LBS), 0.0)
        assertEquals(102.1, displayWeight(kg, 225.0, WeightUnit.LBS, WeightUnit.KG), 1e-9)
    }

    @Test
    fun `barbell ruler reaches 1000 kg`() {
        assertEquals(1000.0, weightRuler(Equipment.BARBELL, WeightUnit.KG).max, 0.0)
        assertEquals(1000.0, toKg(weightRuler(Equipment.BARBELL, WeightUnit.LBS).max, WeightUnit.LBS), 0.2)
    }

    @Test
    fun `resistance grows with weight`() {
        val spec = weightRuler(Equipment.BARBELL, WeightUnit.KG)
        assertEquals(1f, dragResistance(spec, 60.0))
        assertEquals(0.75f, dragResistance(spec, 100.0))
        assertEquals(0.4f, dragResistance(spec, 250.0))
    }

    @Test
    fun `formatting`() {
        assertEquals("102.5", formatWeight(102.5))
        assertEquals("100.0", formatWeight(100.0))
        assertEquals("+2.5", formatSignedWeight(2.5))
    }
}

class OrderLearningTest {
    private val initial = mapOf("A" to 0.0, "B" to 1.0, "C" to 2.0, "D" to 3.0, "E" to 4.0)

    private fun order(scores: Map<String, Double>) = scores.entries.sortedBy { it.value }.map { it.key }

    @Test
    fun `nothing performed leaves the order alone`() {
        assertEquals(initial, learnOrder(initial, emptyList()))
    }

    @Test
    fun `following the current order keeps it`() {
        assertEquals(listOf("A", "B", "C", "D", "E"), order(learnOrder(initial, listOf("A", "B", "C"))))
    }

    @Test
    fun `a consistent new habit takes over within two sessions`() {
        val once = learnOrder(initial, listOf("E", "A"))
        assertTrue("one session should already move E up", order(once).indexOf("E") < 4)
        val twice = learnOrder(once, listOf("E", "A"))
        assertEquals(listOf("E", "A"), order(twice).take(2))
    }

    @Test
    fun `a one-off swap only nudges`() {
        val once = learnOrder(initial, listOf("B", "A", "C", "D", "E"))
        // A and B were adjacent; one swap is enough to flip adjacent items but not to scramble others.
        assertEquals(listOf("C", "D", "E"), order(once).drop(2))
    }

    @Test
    fun `unknown ids are ignored`() {
        assertEquals(order(initial), order(learnOrder(initial, listOf("Z"))))
    }

    @Test
    fun `manual order sets exact positions`() {
        assertEquals(mapOf("C" to 0.0, "A" to 1.0, "B" to 2.0), manualOrderScores(listOf("C", "A", "B")))
    }
}
