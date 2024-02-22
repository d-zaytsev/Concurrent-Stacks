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
        var attempt = 0

        while (true) {
            val expectedValue = head.get()
            val newValue = expectedValue?.next

            if (head.compareAndSet(expectedValue, newValue))
                return expectedValue?.value
            else {
                Thread.sleep(exp.delay(attempt))
                attempt++
                // difference from Treiber stack
                // we will exchange information with PUSH
                val exchanger = randomExchanger()
                val expectedExchanger = exchanger.get()

                if (expectedExchanger.state == ExchangerState.WAITING) // POP only needs WAITING
                {
                    // transform to BUSY
                    if (exchanger.compareAndSet(
                            expectedExchanger,
                            Exchanger(state = ExchangerState.BUSY)
                        )
                    ) // try to update exchanger
                    {
                        return expectedExchanger.value // return value from PUSH or continue
                    }
                }

            }

        }
    }

    override fun push(item: T) {
        var attempt = 0 // cur attempt to read exchanger state
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

                if (expectedExchanger.state == ExchangerState.EMPTY) {
                    //transform to WAITING
                    if (exchanger.compareAndSet(
                            expectedExchanger,
                            Exchanger(value = item, state = ExchangerState.WAITING)
                        )
                    ) {
                        Thread.sleep(exp.delay(attempt))
                        attempt++

                        expectedExchanger = exchanger.get()

                        if (!exchanger.compareAndSet(expectedExchanger, Exchanger(state = ExchangerState.EMPTY)))
                            return
                        if (expectedExchanger.state == ExchangerState.BUSY)
                            return

                    }
                }
            }

        }
    }

}