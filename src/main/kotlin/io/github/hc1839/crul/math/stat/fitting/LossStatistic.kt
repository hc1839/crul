package io.github.hc1839.crul.math.stat.fitting

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic

/**
 *  Statistic of the values of a loss function.
 */
interface LossStatistic : UnivariateStatistic {
    /**
     *  Loss function.
     *
     *  @param predicted
     *      Predicted value.
     *
     *  @param observed
     *      Observed value.
     *
     *  @return
     *      Value of the cost.
     */
    fun lossFunction(predicted: Double, observed: Double): Double

    /**
     *  Evaluates the statistic of the loss function over paired predicted and
     *  observed values.
     *
     *  @param predObsValues
     *      Non-empty list of pairs of predicted and observed values.
     *
     *  @return
     *      Value of the statistic applied to the loss function over
     *      `predObsValues`.
     */
    fun evaluatePredObsValues(
        predObsValues: List<Pair<Double, Double>>
    ): Double
    {
        if (predObsValues.isEmpty()) {
            throw IllegalArgumentException(
                "List of pairs of predicted and observed values is empty."
            )
        }

        return evaluate(
            predObsValues.map { (pred, obs) ->
                lossFunction(pred, obs)
            }.toDoubleArray()
        )
    }

    /**
     *  Evaluates the statistic of an array of values of the loss function.
     *
     *  @param values
     *      Non-empty array of values of the loss function.
     *
     *  @return
     *      Value of the statistic applied to `values`.
     */
    override fun evaluate(values: DoubleArray): Double {
        if (values.isEmpty()) {
            throw IllegalArgumentException(
                "Array of values of the loss function is empty."
            )
        }

        return evaluate(values, 0, values.count())
    }

    /**
     *  Evaluates the statistic over specified elements of an array of values
     *  of the loss function.
     *
     *  @param values
     *      Non-empty array of values of the loss function.
     *
     *  @param begin
     *      Index of the first element to include.
     *
     *  @param length
     *      Number of elements to include.
     *
     *  @return
     *      Value of the statistic applied to the included elements of
     *      `values`.
     */
    abstract override fun evaluate(
        values: DoubleArray,
        begin: Int,
        length: Int
    ): Double
}
