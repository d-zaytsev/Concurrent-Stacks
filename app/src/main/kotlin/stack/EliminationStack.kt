package stack

import java.util.concurrent.atomic.AtomicReference

/**
 * States of exchangers
 */
private enum class ExchangerState {
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
class EliminationStack<T>(capacity: Int, private val maxAttempts: Int) : TreiberStack<T>() {

    override val head = AtomicReference<StackNode<T>?>(null)

    // We need an elimination array. It will be used to exchange information between threads.
    private val exchangersArray = Array(capacity) { AtomicReference(Exchanger<T>()) }
    private fun randomExchanger() = exchangersArray.random()
    override fun pop(): T? {
        while (true) {
            val stackRes = tryPerformStackOp()
            if (stackRes.first) // success with CAS
                return stackRes.second // value

            for (attempt in 0 until maxAttempts) {
                val exchanger = randomExchanger()
                val expected = exchanger.get()

                if (expected.state == ExchangerState.EMPTY) {
                    if (tryCollision(exchanger, attempts = maxAttempts - attempt)) {
                        val item = exchanger.get().value
                        if (!finishCollision(exchanger)) throw Exception("Someone clear my BUSY :(")
                        return item
                    }
                } else if (expected.state == ExchangerState.WAITING && expected.value != null) {
                    if (exchanger.compareAndSet(expected, Exchanger(state = ExchangerState.BUSY)))
                        return expected.value
                }

            }
        }

    }

    override fun push(item: T) {
        while (true) {
            if (tryPerformStackOp(item)) return

            val exchanger = randomExchanger()
            val expected = exchanger.get()

            for (attempt in 0 until maxAttempts) {
                if (expected.state == ExchangerState.EMPTY) {
                    if (tryCollision(exchanger, value = item, attempts = maxAttempts - attempt)) {
                        if (!finishCollision(exchanger)) throw Exception("Someone clear my BUSY (((")
                        return
                    }
                } else if (expected.state == ExchangerState.WAITING && expected.value == null) {
                    if (exchanger.compareAndSet(expected, Exchanger(state = ExchangerState.BUSY, value = item)))
                        return
                }
            }
        }
    }

    /**
     * default POP with CAS
     */
    private fun tryPerformStackOp(): Pair<Boolean, T?> {
        val expectedValue = head.get() ?: return Pair(true, null)
        val newValue = expectedValue.next

        return if (head.compareAndSet(expectedValue, newValue)) Pair(true, expectedValue.value) else Pair(false, null)
    }

    /**
     * default PUSH with CAS
     */
    private fun tryPerformStackOp(value: T): Boolean {
        val expectedValue = head.get()
        val newValue = StackNode(value, expectedValue)

        return head.compareAndSet(expectedValue, newValue)
    }

    /**
     * wait for BUSY
     */
    private fun tryCollision(
        randomExchanger: AtomicReference<Exchanger<T>>,
        value: T? = null,
        attempts: Int
    ): Boolean {

        // set WAITING
        var exchanger = randomExchanger.get()
        if (exchanger.state != ExchangerState.EMPTY)
            return false
        if (!randomExchanger.compareAndSet(
                exchanger,
                Exchanger(value = value, state = ExchangerState.WAITING)
            )
        )
            return false

        repeat(attempts) {
            exchanger = randomExchanger.get()
            if (exchanger.state == ExchangerState.BUSY)
                return true
        }
        // try to return back
        return !randomExchanger.compareAndSet(exchanger, Exchanger(state = ExchangerState.EMPTY))
    }

    /**
     * Clear BUSY
     */
    private fun finishCollision(randomExchanger: AtomicReference<Exchanger<T>>): Boolean {
        val exchanger = randomExchanger.get()
        if (exchanger.state != ExchangerState.BUSY)
            return false
        return randomExchanger.compareAndSet(exchanger, Exchanger(state = ExchangerState.EMPTY))
    }
}