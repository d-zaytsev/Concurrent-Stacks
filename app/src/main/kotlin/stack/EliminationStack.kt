package stack

import java.util.concurrent.atomic.AtomicReference

/**
 * States of exchangers
 */
private enum class ExchangerState() {
    EMPTY, // Can be used for data transferring
    WAITING, // PUSH is waiting for POP
    BUSY // POP had done its job, it needs to be cleaned
}

/**
 * Class for exchanging information between cells of array
 */
private class Exchanger<T>(val value: T? = null, val state: ExchangerState = ExchangerState.EMPTY)

/**
 * Lock-free elimination back-off stack.
 */
class EliminationStack<T>(capacity: Int, startDelay: Long, maxDelay: Long) : TreiberStack<T>() {

    override val head = AtomicReference<StackNode<T>?>(null)

    // Backoff
    private val exp = ExponentialStrategy(startDelay, maxDelay)

    // We need an elimination array. It will be used to exchange information between threads.
    private val exchangersArray = Array(capacity) { AtomicReference(Exchanger<T>()) }
    private fun randomExchanger() = exchangersArray.random()
    override fun pop(): T? {
        while (true) {
            val expectedValue = head.get()
            val newValue = expectedValue?.next

            if (head.compareAndSet(expectedValue, newValue))
                return expectedValue?.value
            else {
                // difference from Treiber stack
                // we will exchange information with PUSH
                val exchanger = randomExchanger()
                var expectedExchanger = exchanger.get()

                if (expectedExchanger.state == ExchangerState.BUSY) // POP only needs WAITING
                    continue
                else if (expectedExchanger.state == ExchangerState.EMPTY) {
                    // try to wait
                    var attempt = 0 // cur attempt to read exchanger state
                    while (true) {
                        expectedExchanger = exchanger.get() // our exchanger can change O_O
                        if (expectedExchanger.state == ExchangerState.WAITING) // wait for WAITING
                            break

                        val delay = exp.delay(attempt) //calculate delay
                        if (delay >= exp.maxDelay) {
                            // exit from loop, we can't wait more
                            break
                        } else {
                            // or we can wait more
                            Thread.sleep(delay)
                            attempt++
                        }
                    }
                } else {
                    // transform to BUSY
                    if (exchanger.compareAndSet(
                            expectedExchanger,
                            Exchanger(state = ExchangerState.BUSY)
                        )
                    ) // try to update exchanger
                        return expectedExchanger.value // return value from PUSH
                }
            }
        }
    }

    override fun push(item: T) {
        while (true) {
            val expectedValue = head.get()
            val newValue = StackNode(item, expectedValue)

            if (head.compareAndSet(expectedValue, newValue))
                return
            else {
                // difference from Treiber stack
                // we will exchange information with POP
                val exchanger = randomExchanger()
                var expectedExchanger = exchanger.get()

                if (expectedExchanger.state != ExchangerState.EMPTY)
                    return

                //transform to WAITING
                if (exchanger.compareAndSet(
                        expectedExchanger,
                        Exchanger(value = item, state = ExchangerState.WAITING)
                    )
                ) {
                    // waiting corresponding POP operation for exchange
                    var attempt = 0 // cur attempt to read exchanger state
                    while (true) {
                        expectedExchanger = exchanger.get() // our exchanger can change O_O
                        if (expectedExchanger.state == ExchangerState.BUSY) // wait for BUSY
                            break

                        // if no BUSY
                        val delay = exp.delay(attempt) //calculate delay
                        if (delay >= exp.maxDelay) {
                            // exit from loop, we can't wait more
                            if (exchanger.compareAndSet(expectedExchanger, Exchanger(state = ExchangerState.EMPTY)))
                                break // if state was changed -> continue
                        } else {
                            // or we can wait more
                            Thread.sleep(delay)
                            attempt++
                        }
                    }
                    // clean
                    exchanger.compareAndSet(expectedExchanger, Exchanger(state = ExchangerState.EMPTY))
                }
            }
        }
    }

}