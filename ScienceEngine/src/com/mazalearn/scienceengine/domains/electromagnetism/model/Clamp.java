package com.mazalearn.scienceengine.domains.electromagnetism.model;

public class Clamp {

  /**
   * Clamps a value to a specified range.
   * 
   * @param min - the minimum value
   * @param value - the value to be clamped
   * @param max - the maximum value
   * @return the clamped value
   */
  public static float clamp(float min, float value, float max) {
    if (Float.isNaN(min) || Float.isNaN(value) || Float.isNaN(max)) {
      return Float.NaN;
    } else if (value < min) {
      return min;
    } else if (value > max) {
      return max;
    }
    return value;
  }

}
