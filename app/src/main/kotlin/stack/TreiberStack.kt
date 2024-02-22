package stack

import java.util.concurrent.atomic.AtomicReference

class StackNode<T>(val value: T, val next: StackNode<T>?)

/**
 * Lock-free stack implementation
 */
open class TreiberStack<T> {
    protected open val head = AtomicReference<StackNode<T>?>(null)

    /*
     * PUSH and POP use a loop, which affects performance.
     * This stack doesn't scale well to a large number of threads.
     */

    open fun pop(): T? {
        while (true) {
            val expectedValue = head.get() // What we expect the head will be
            val newValue = expectedValue?.next

            if (head.compareAndSet(expectedValue, newValue)) // if (what we expect) = (what we have)
                return expectedValue?.value
        }
    }

    open fun push(item: T) {
        while (true) {
            val expectedValue = head.get()
            val newValue = StackNode(item, expectedValue)

            if (head.compareAndSet(expectedValue, newValue))
                return
        }
    }
    fun peak() = head.get()?.value
    fun empty() = head.get() == null
    private fun StackNode<T>.print() : String = "$value" + if (next != null) " -> ${next.print()}" else "."
    override fun toString(): String = head.get()?.print() ?: "Empty stack"
}