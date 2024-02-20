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
 * Just a node, nothing unusual
 */
private class StackNode<T>(val value: T, val next: StackNode<T>?)

/**
 * Lock-free elimination back-off stack.
 */
class EliminationStack<T>(val capacity: Int) {

    private val head = AtomicReference<StackNode<T>?>(null)

    // We need an elimination array. It will be used to exchange information between threads.
    private val exchangersArray = Array(capacity) { AtomicReference<Exchanger<T>>() }
    private fun randomExchanger() = exchangersArray.random()
    fun Pop(): T? {
        while (true) {
            val expectedValue = head.get()
            val newValue = expectedValue?.next

            if (head.compareAndSet(expectedValue, newValue))
                return expectedValue?.value
            else {
                // difference from Treiber stack
                // we will exchange information with PUSH
                val exchanger = randomExchanger()
                val expectedExchanger = exchanger.get()

                if (expectedExchanger.state != ExchangerState.WAITING) // POP only needs WAITING
                    continue;

                val newExchanger = Exchanger<T>(state = ExchangerState.BUSY)

                if (exchanger.compareAndSet(expectedExchanger, newExchanger)) // try to update exchanger
                    return expectedExchanger.value // return value from PUSH
            }
        }
    }

    fun Push(item: T) {
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

                val newExchanger = Exchanger(value = item, state = ExchangerState.WAITING)

                if (exchanger.compareAndSet(expectedExchanger, newExchanger)) {
                    // waiting corresponding POP operation for exchange
                    while (true) {
                        expectedExchanger = exchanger.get()
                        if (expectedExchanger.state == ExchangerState.BUSY)
                            break
                    }

                    // clean
                    val newExchanger = Exchanger<T>(state = ExchangerState.EMPTY)
                    exchanger.compareAndSet(expectedExchanger, newExchanger)
                }
            }
        }
    }

}