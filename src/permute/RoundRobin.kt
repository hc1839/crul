package crul.permute

import crul.permute.variation.ListItemVariator

/**
 *  List partitioning by round robin.
 */
object RoundRobin {
    /**
     *  Partitions a list into a list of lists by round robin.
     *
     *  @param elements
     *      Non-empty list to partition.
     *
     *  @param partitionCount
     *      Number of partitions. It is a positive number that is less than or
     *      equal to the number of elements.
     *
     *  @return
     *      List of partitions.
     */
    @JvmStatic
    fun <T> partition(elements: List<T>, partitionCount: Int): List<List<T>> {
        if (elements.isEmpty()) {
            throw IllegalArgumentException(
                "List to partition is empty."
            )
        }

        if (partitionCount < 1 || partitionCount > elements.count()) {
            throw IllegalArgumentException(
                "Invalid number of partitions: $partitionCount"
            )
        }

        val partitions = (0 until partitionCount).map { mutableListOf<T>() }
        var partitionIndexVariator = ListItemVariator(partitions.indices)

        for (item in elements) {
            partitions[partitionIndexVariator++.value()].add(item)
        }

        return partitions.map { it.toList() }
    }
}