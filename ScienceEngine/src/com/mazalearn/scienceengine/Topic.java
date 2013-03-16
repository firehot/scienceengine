package com.mazalearn.scienceengine;


public enum Topic {
  Field,
  BarMagnet, TwoWires, ElectroMagnet,
  BarMagnetInduction, ElectroMagnetInduction,
  DCMotor, ACMotor, ScienceTrain, EMReview,
  Electromagnetism(BarMagnet, Field, BarMagnet, TwoWires, ElectroMagnet, BarMagnetInduction, ElectroMagnetInduction, DCMotor,
      ACMotor, ScienceTrain, EMReview),
  SOM, StatesOfMatter(SOM, SOM), 
  W, Waves(W, W);

  private Topic[] childTopics;
  private Topic canonicalChild;

  Topic(Topic... childTopics) {
    if (childTopics.length == 0) {
      this.childTopics = childTopics;
      return;
    }
    this.canonicalChild = childTopics[0];
    this.childTopics = new Topic[childTopics.length - 1];
    for (int i = 1; i < childTopics.length; i++) {
      this.childTopics[i - 1] = childTopics[i];
    }
  }
  
  public Topic getCanonicalChild() {
    return canonicalChild;
  }
  
  public Topic[] getChildren() {
    return childTopics;
  }

}
