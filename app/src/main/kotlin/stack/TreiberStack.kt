package stack

import java.util.concurrent.atomic.AtomicReference

private class Node<T>(val value: T, val next: Node<T>?)

/**
 * Lock-free stack implementation
 */
open class TreiberStack<T> {
    private val head: AtomicReference<Node<T>?> = AtomicReference(null)

    /**
     * PUSH and POP use a loop, which affects perfomance.
     * This stack doesn't scale well to a large number of threads.
     * */

    fun pop(): T? {
        while (true) {
            val expectedValue = head.get() // What we expect the head will be
            val newValue = expectedValue?.next

            if (head.compareAndSet(expectedValue, newValue)) // if (what we expect) = (what we have)
                return expectedValue?.value
        }
    }

    fun push(item: T) {
        while (true) {
            val expectedValue = head.get()
            val newValue = Node(item, expectedValue)

            if (head.compareAndSet(expectedValue, newValue))
                return
        }
    }
    fun peak() = head.get()?.value
    fun empty() = head.get() == null
    private fun Node<T>.print() : String = "$value" + if (next != null) " -> ${next.print()}" else "."
    override fun toString(): String = head.get()?.print() ?: "Empty stack"
}