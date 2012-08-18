package com.mazalearn.scienceengine.experiments.model.electromagnetism;

public class Util {

  /**
   * Clamps a value to a specified range.
   * 
   * @param min
   *          the minimum value
   * @param value
   *          the value to be clamped
   * @param max
   *          the maximum value
   * @return the clamped value
   */
  public static double clamp(double min, double value, double max) {
    if (Double.isNaN(min) || Double.isNaN(value) || Double.isNaN(max)) {
      return Double.NaN;
    } else if (value < min) {
      return min;
    } else if (value > max) {
      return max;
    }
    return value;
  }

}
