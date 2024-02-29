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
class EliminationStack<T>(capacity: Int, private val maxDelay: Long) : TreiberStack<T>() {

    override val head = AtomicReference<StackNode<T>?>(null)

    // We need an elimination array. It will be used to exchange information between threads.
    private val exchangersArray = Array(capacity) { AtomicReference(Exchanger<T>()) }
    private fun randomExchanger() = exchangersArray.random()
    override fun pop(): T? {

        while (true) {
            val expectedValue = head.get()
            val newValue = expectedValue?.next

            // each thread tries to perform its operation on the central stack object
            if (head.compareAndSet(expectedValue, newValue))
                return expectedValue?.value
            else {

                // if this attempt fails,
                // thread goes through the collision layer
                val exchanger = randomExchanger() // choose random location in array
                val expectedExchanger = exchanger.get()

                if (expectedExchanger.state == ExchangerState.WAITING) // two threads can collide only if they have opposing operations
                {
                    // tries to change state
                    if (exchanger.compareAndSet(
                            expectedExchanger,
                            Exchanger(state = ExchangerState.BUSY)
                        )
                    ) {
                        return expectedExchanger.value // return value from PUSH
                        //If it fails, it retries until success
                    }
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

                if (expectedExchanger.state == ExchangerState.EMPTY) {
                    //transform to WAITING
                    if (exchanger.compareAndSet(
                            expectedExchanger,
                            Exchanger(value = item, state = ExchangerState.WAITING)
                        )
                    ) {

                        Thread.sleep(30)

                        expectedExchanger = exchanger.get()

                        if (expectedExchanger.state == ExchangerState.BUSY) {
                            if (!exchanger.compareAndSet(
                                    expectedExchanger,
                                    Exchanger(state = ExchangerState.EMPTY)
                                )
                            ) {
                                throw IllegalStateException("Someone update my BUSY item -_-")
                            }

                            return // complete collide

                        } else {
                            if (!exchanger.compareAndSet(
                                    expectedExchanger,
                                    Exchanger(state = ExchangerState.EMPTY)
                                )
                            ) {
                                // If our entry cannot be cleared, it follows
                                // that our thread has been collided with
                                if (!exchanger.compareAndSet(
                                        expectedExchanger,
                                        Exchanger(state = ExchangerState.EMPTY)
                                    )
                                )
                                    throw IllegalStateException("Someone update my BUSY item -_-")
                                return
                            } else {
                                // If no other thread
                                // collides with our thread during its waiting period,
                                // we clear the elimination array and start from the beginning
                            }
                        }

                    }
                }
            }

        }
    }

}