package org.example

import benchmark.ProduceConsumeBenchmark
import stack.EliminationStack
import stack.TreiberStack
import kotlin.math.roundToInt

fun main() {

    // Stacks settings
    val threadCount = 8

    // Tests settings
    val time = 2000L
    val workload = 100L
    val repeats = 5

    val stackArr = arrayOf(TreiberStack<Int>(), EliminationStack(8, 20))
    println("Stack 1: Treibers Stack (without backoff)\nStack 2: Elimination Backoff Stack\n")

    for (i in stackArr.indices) {
        println("### Stack ${i + 1}: ")

        val bench = ProduceConsumeBenchmark(stackArr[i], workload)

        val results = Array(repeats) {
            bench.perform(time, threadCount)
        }

        println("result: ${results.average().roundToInt()} ops")
    }
}
