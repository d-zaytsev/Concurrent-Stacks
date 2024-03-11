package benchmark

import stack.TreiberStack
import kotlin.concurrent.thread
import kotlin.random.Random

class ProduceConsumeBenchmark(private val stack: TreiberStack<Int>, private val workload: Long) {

    // each thread alternately performs a push or pop operation and then
    // waits for a period or time (choose randomly in range [0...workload])
    init {
        require(workload > 0)
        require(stack.empty())
    }

    @Volatile
    var run = false

    fun perform(time: Long, threadCount: Int): Int {
        run = false
        val operationsArray = Array(threadCount) { 0 }

        val threadArray = Array(threadCount) {
            thread(start = true) {
                while (!run) {
                }

                while (run) {
                    stack.push(1)
                    stack.pop()
                    operationsArray[it] += 2
                    Thread.sleep(Random.nextLong(0, workload))
                }
            }
        }

        // Starting all threads
        run = true

        Thread.sleep(time)
        // Wait for all threads to finish
        run = false
        threadArray.forEach { it.join() }

        return operationsArray.sum()

    }
}