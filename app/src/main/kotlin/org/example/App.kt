package org.example

import benchmark.ProduceConsumeBenchmark
import stack.EliminationStack
import stack.TreiberStack
import kotlin.math.roundToInt

fun main() {

    // Stacks settings
    val threadCount = 16
    val eliminationStackDelay = 100L

    // Tests settings
    val time = 1000L
    val workload = 100L
    val repeats = 5

    println("\n### Elimination Stack: ")

    val eliminationStack = EliminationStack<Int>(threadCount, eliminationStackDelay)
    val eliminationBenchmark = ProduceConsumeBenchmark(eliminationStack, workload)

    val results = Array(repeats) {
        eliminationBenchmark.perform(time, threadCount)
    }

    println("result: ${results.average().roundToInt()} ops")

    println("### Treiber Stack: ")

    val treiberStack = TreiberStack<Int>()
    val treiberBenchmark = ProduceConsumeBenchmark(treiberStack, workload)

    val treiberResults = Array(repeats) {
        treiberBenchmark.perform(time, threadCount)
    }

    println("result: ${treiberResults.average().roundToInt()} ops")
}
