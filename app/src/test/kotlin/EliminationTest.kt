
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.StateRepresentation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.junit.Assert
import org.junit.Test
import stack.EliminationStack

class EliminationTest {

    private val threadCount = 2
    private val stack = EliminationStack<Int>(threadCount, 1, 500)

    @Operation
    fun pop() = if (!stack.empty()) stack.pop() else null

    @Operation
    fun push(item: Int) = stack.push(item)

    @StateRepresentation
    fun stateRepresentation() = stack.toString()

    @Test
    fun modelCheck() = ModelCheckingOptions()
        .threads(threadCount)
        .actorsPerThread(2)
        .iterations(5)
        .check(this::class)

    @Test
    fun progressCheck() = ModelCheckingOptions().checkObstructionFreedom().check(this::class)


    @Test
    fun `Simple test`() {
        val s = EliminationStack<Int>(100, 100, 100)
        s.push(1)
        s.push(2)
        s.push(3)
        s.pop()
        s.pop()

        Assert.assertEquals(1, s.pop())
    }

    @Test
    fun `Representation test`() {
        val s = EliminationStack<Int>(100, 100, 100)
        s.push(1)
        s.push(2)
        s.push(3)

        Assert.assertEquals("3 -> 2 -> 1.", s.toString())
    }

    @Test
    fun `Null test`() {
        val s = EliminationStack<Int>(100, 100, 100)
        Assert.assertEquals(null, s.pop())
    }
}