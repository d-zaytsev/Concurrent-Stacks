package stack

import java.util.*
import java.util.concurrent.atomic.AtomicReference

private class Node<T>(val value: T, val next: Node<T>)

class TreibersStack<T> {
    private val head: Node<T>? = null

    fun pop(): T? {
        val curHead = AtomicReference(head) // An object reference that is always updated atomically

        while (true) {
            val expectedValue = curHead.get() // What we expect the head will be like
            val newValue = expectedValue?.next ?: throw EmptyStackException()

            if (curHead.compareAndSet(expectedValue, newValue)) // if (what we expect) = (what we have)
                return newValue.value
        }
    }

    fun push(item: T) {
        val curHead = AtomicReference(head)

        while (true) {
            val expectedValue = curHead.get() ?: throw EmptyStackException()
            val newValue = Node(item, expectedValue)

            if (curHead.compareAndSet(expectedValue, newValue))
                return
        }
    }
    fun peak() = head?.value
    fun empty() = head == null

}