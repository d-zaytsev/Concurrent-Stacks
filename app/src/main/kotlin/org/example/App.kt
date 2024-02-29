package org.example

import benchmark.ProduceConsumeBenchmark
import stack.EliminationStack
import stack.TreiberStack
import kotlin.time.measureTime

fun main() {

    val threadCountArray = arrayOf(32)
    val operationsCount = 5000
    val workload = 100L
    val repeats = 1

    println("### Treiber Stack: ")

    repeat(repeats) {
        println(" --- ${it+1} try ---")

        for (threadCount in threadCountArray) {
            val stack = TreiberStack<Int>()
            val benchmark = ProduceConsumeBenchmark(stack, workload)

            val timeTaken = measureTime {
                benchmark.perform(operationsCount, threadCount)
            }

            println("$threadCount: $timeTaken")
        }
    }

    println("\n### Elimination Stack: ")

    repeat(repeats) {
        println(" --- ${it+1} try ---")

        for (threadCount in threadCountArray) {
            val stack = EliminationStack<Int>(32, 1)
            val benchmark = ProduceConsumeBenchmark(stack, workload)

            val timeTaken = measureTime {
                benchmark.perform(operationsCount, threadCount)
            }

            println("$threadCount: $timeTaken")
        }
    }
}
