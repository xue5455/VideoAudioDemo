package com.xue.douyin.common.recorder.audio;

/**
 * Created by 薛贤俊 on 2018/3/23.
 */

public class Util {
    /**
     * Scales a large timestamp.
     * <p>
     * Logically, scaling consists of a multiplication followed by a division. The actual operations
     * performed are designed to minimize the probability of overflow.
     *
     * @param timestamp The timestamp to scale.
     * @param multiplier The multiplier.
     * @param divisor The divisor.
     * @return The scaled timestamp.
     */
    public static long scaleLargeTimestamp(long timestamp, long multiplier, long divisor) {
        if (divisor >= multiplier && (divisor % multiplier) == 0) {
            long divisionFactor = divisor / multiplier;
            return timestamp / divisionFactor;
        } else if (divisor < multiplier && (multiplier % divisor) == 0) {
            long multiplicationFactor = multiplier / divisor;
            return timestamp * multiplicationFactor;
        } else {
            double multiplicationFactor = (double) multiplier / divisor;
            return (long) (timestamp * multiplicationFactor);
        }
    }

}
