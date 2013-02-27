package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;

public class Utils {

  /**
   * Permute given list and return the permutation
   * @param list
   * @return
   */
  public static <T> int[] shuffle(List<T> list) {
    int[] permutation = new int[list.size()];
    for (int i = 0; i < permutation.length; i++) {
      permutation[i] = i;
    }
    
    for (int i = list.size() - 1; i >= 1; i--) {
      T tmp = list.get(i);
      int j = MathUtils.random(i);
      list.set(i, list.get(j));
      list.set(j, tmp);
      int t = permutation[i];
      permutation[i] = permutation[j];
      permutation[j] = t;
    }
    return permutation;
  }

}
