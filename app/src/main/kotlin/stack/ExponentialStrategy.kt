package stack

import kotlin.math.pow

/**
 * For an exponential backoff algorithm
 */
class ExponentialStrategy(private val startDelay: Long, val maxDelay: Long, private val base: Double = 2.0) {

    init {
        require(startDelay > 0)
        require(maxDelay > 0)
        require(base >= 2.0)
    }

    /**
     * Calculates next delay
     * @param c current attempts count
     * @return time delay applied between actions
     */
    fun delay(c: Int): Long = kotlin.math.min(maxDelay, kotlin.math.max(startDelay, base.pow(c).toLong()))
}