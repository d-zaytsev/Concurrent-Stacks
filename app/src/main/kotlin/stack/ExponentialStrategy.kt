package stack

import kotlin.math.max
import kotlin.math.pow

/**
 * For an exponential backoff algorithm
 */
class ExponentialStrategy(private val maxDelay: Long, private val base: Double = 2.0) {

    private var attempt = 0
    private var delay = 0L

    init {
        require(maxDelay > 0)
        require(base >= 2.0)
    }

    /**
     * Calculates next delay
     * @return time delay applied between actions
     */
    fun nextDelay(): Long {
        attempt++
        delay = base.pow(attempt).toLong()
        return max(maxDelay, delay)
    }

    fun isMaxDelay(): Boolean = delay >= maxDelay
}