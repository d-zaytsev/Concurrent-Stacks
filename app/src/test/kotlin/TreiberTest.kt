import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.StateRepresentation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.Assert
import org.junit.Test
import stack.TreiberStack

class TreiberTest {
    private val stack = TreiberStack<Int>()

    @Operation
    fun pop() = stack.pop()

    @Operation
    fun push(item: Int) = stack.push(item)

    @StateRepresentation
    fun stateRepresentation() = stack.toString()

    @Test
    fun modelCheck() =
        ModelCheckingOptions().checkObstructionFreedom().sequentialSpecification(SequentialStack::class.java)
            .check(this::class)

    @Test
    fun stressCheck() = StressOptions().sequentialSpecification(SequentialStack::class.java).check(this::class)

    @Test
    fun `Simple test`() {
        val s = TreiberStack<Int>()
        s.push(1)
        s.push(2)
        s.push(3)

        Assert.assertEquals(3, s.pop())
        Assert.assertEquals(2, s.pop())
        Assert.assertEquals(1, s.pop())
    }

    @Test
    fun `Representation test`() {
        val s = TreiberStack<Int>()
        s.push(1)
        s.push(2)
        s.push(3)

        Assert.assertEquals("3 -> 2 -> 1.", s.toString())
    }

    @Test
    fun `Null test`() {
        val s = TreiberStack<Int>()
        Assert.assertEquals(null, s.pop())
    }
}