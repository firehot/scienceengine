package com.mazalearn.scienceengine;


public enum Topic {
  Field,
  BarMagnet, TwoWires, ElectroMagnet,
  BarMagnetInduction, ElectroMagnetInduction,
  DCMotor, ACMotor, ScienceTrain, EMReview,
  Electromagnetism(Field, BarMagnet, TwoWires, ElectroMagnet, BarMagnetInduction, ElectroMagnetInduction, DCMotor,
      ACMotor, ScienceTrain, EMReview),
  SOM, StatesOfMatter(SOM), 
  W, Waves(W);

  private Topic[] childTopics;

  Topic(Topic... topics) {
    this.childTopics = topics;
  }
  
  public Topic[] getChildren() {
    return childTopics;
  }

}
