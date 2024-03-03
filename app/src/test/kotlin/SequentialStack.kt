class SequentialStack {
    private val stack = ArrayDeque<Int>()

    fun push(item: Int) = stack.addLast(item)

    fun pop() = stack.removeLastOrNull()
}