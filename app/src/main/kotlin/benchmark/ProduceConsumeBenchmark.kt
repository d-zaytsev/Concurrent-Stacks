package benchmark

import stack.TreiberStack
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.random.Random

class ProduceConsumeBenchmark(private val stack: TreiberStack<Int>, private val workload: Long) {

    // each thread alternately performs a push or pop operation and then
    // waits for a period or time (choose randomly in range [0...workload])
    init {
        require(workload > 0)
        require(stack.empty())
    }

    fun perform(operationsCount: Int, threadCount: Int) {
        val atomicCounter = AtomicInteger()
        val threadArray = Array(threadCount) {
            thread(start = false) {
                while (atomicCounter.get() < operationsCount) {
                    stack.push(1)
                    stack.pop()
                    atomicCounter.addAndGet(2)
                    Thread.sleep(Random.nextLong(0, workload))
                }
            }
        }

        // Starting all threads
        threadArray.forEach { it.start() }

        // Wait for all threads to finish
        threadArray.forEach { it.join() }

    }

//    fun perform(time: Duration, threadCount: Int): Int {
//
//    }
}