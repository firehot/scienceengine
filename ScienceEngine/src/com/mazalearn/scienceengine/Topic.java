package com.mazalearn.scienceengine;

public enum Topic {
  Electromagnetism(9), StatesOfMatter(1), Waves(1);

  private int numLevels;

  Topic(int numLevels) {
    this.numLevels = numLevels;
  }
  
  public int getNumLevels() {
    return numLevels;
  }

}
