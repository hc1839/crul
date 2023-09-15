/*
 *  Copyright Han Chen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package io.github.hc1839.crul.permute

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
        var partitionIndex = 0

        for (element in elements) {
            partitions[partitionIndex].add(element)

            if (partitionIndex != partitions.count()) {
                ++partitionIndex
            } else {
                partitionIndex = 0
            }
        }

        return partitions.map { it.toList() }
    }
}
