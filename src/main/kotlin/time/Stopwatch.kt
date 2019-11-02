package crul.time

import java.time.*
import java.time.temporal.*

/**
 *  Stopwatch.
 */
class Stopwatch() {
    /**
     *  Lap start times.
     */
    private val lapStartTimes: MutableList<ZonedDateTime> =
        mutableListOf()

    /**
     *  Lap end times.
     */
    private val lapEndTimes: MutableList<ZonedDateTime> =
        mutableListOf()

    /**
     *  Whether the stopwatch is running.
     */
    val isRunning: Boolean
        get() = lapStartTimes.count() > lapEndTimes.count()

    /**
     *  Resets the stopwatch by clearing all lap times.
     *
     *  If the stopwatch is running, an exception is raised.
     */
    fun reset() {
        if (isRunning) {
            throw RuntimeException(
                "Stopwatch is running."
            )
        }

        lapStartTimes.clear()
        lapEndTimes.clear()
    }

    /**
     *  Starts the stopwatch.
     *
     *  If the stopwatch is running, nothing is done.
     */
    fun start() {
        if (isRunning) {
            return
        }

        lapStartTimes.add(ZonedDateTime.now(ZoneOffset.UTC))
    }

    /**
     *  Stops the stopwatch.
     *
     *  If the stopwatch is not running, nothing is done.
     */
    fun stop() {
        if (!isRunning) {
            return
        }

        lapEndTimes.add(ZonedDateTime.now(ZoneOffset.UTC))
    }

    /**
     *  Elapsed times of the laps that the stopwatch has measured.
     *
     *  The last element in the returned list corresponds to the most recent
     *  lap. If the stopwatch is running, the current time is used as the end
     *  time for the current lap without stopping the stopwatch.
     */
    fun elapsedTimes(): List<Duration> =
        lapStartTimes.zip(
            if (lapEndTimes.count() < lapStartTimes.count()) {
                lapEndTimes.plusElement(
                    ZonedDateTime.now(ZoneOffset.UTC)
                )
            } else {
                lapEndTimes
            }
        ) { startTime, endTime ->
            Duration.between(startTime, endTime)
        }
}
