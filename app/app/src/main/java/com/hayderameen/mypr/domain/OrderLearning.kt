package com.hayderameen.mypr.domain

/**
 * Learns the order a lifter actually performs a day's exercises.
 *
 * Every exercise in a day has an `orderScore`; the list is sorted by it (lowest first).
 * When a workout ends we build the "observed order": exercises in the order they were first
 * performed, followed by the ones that were skipped (keeping their current relative order).
 * Each score then moves [LEARNING_RATE] of the way toward its observed position.
 *
 * With a rate of 0.6 a consistent new habit takes over within ~2 sessions, while a one-off
 * deviation (e.g. the bench was busy) only nudges the list.
 */
const val LEARNING_RATE = 0.6

fun learnOrder(
    currentScores: Map<String, Double>,
    performedInOrder: List<String>,
    rate: Double = LEARNING_RATE,
): Map<String, Double> {
    val performed = performedInOrder.filter { it in currentScores }.distinct()
    if (performed.isEmpty()) return currentScores
    val performedSet = performed.toSet()
    val skipped = currentScores.entries
        .filter { it.key !in performedSet }
        .sortedBy { it.value }
        .map { it.key }
    val observed = performed + skipped
    return observed.withIndex().associate { (position, id) ->
        val old = currentScores.getValue(id)
        id to (old * (1 - rate) + position * rate)
    }
}

/** Scores for a manual reorder: the user's explicit order wins outright. */
fun manualOrderScores(orderedIds: List<String>): Map<String, Double> =
    orderedIds.withIndex().associate { (i, id) -> id to i.toDouble() }
