package org.example

import benchmark.ProduceConsumeBenchmark
import stack.EliminationStack
import stack.TreiberStack

fun main() {

    val threadCountArray = arrayOf(8)
    val time = 1000L
    val workload = 100L
    val repeats = 1

    println("### Treiber Stack: ")

    repeat(repeats) {
        println(" --- ${it + 1} try ---")

        for (threadCount in threadCountArray) {
            val stack = TreiberStack<Int>()
            val benchmark = ProduceConsumeBenchmark(stack, workload)

            println("$threadCount: ${benchmark.perform(time, threadCount)}")
        }
    }

    println("\n### Elimination Stack: ")

    repeat(repeats) {
        println(" --- ${it + 1} try ---")

        for (threadCount in threadCountArray) {
            val stack = EliminationStack<Int>(32, 1)
            val benchmark = ProduceConsumeBenchmark(stack, workload)

            println("$threadCount: ${benchmark.perform(time, threadCount)}")
        }
    }
}
