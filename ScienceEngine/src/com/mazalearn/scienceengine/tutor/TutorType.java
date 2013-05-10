package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.tutor.ITutor.ITutorType;

public enum TutorType implements ITutorType {
  Root(Color.YELLOW, 0, 0), 
  Guide(Color.YELLOW, 0, 0), 
  Challenge(Color.RED, 0, 0), 
  RapidFire(Color.MAGENTA, 0, 0),
  MCQ1(Color.MAGENTA, 10, 0),
  MCQ(Color.MAGENTA, 10, 0),
  KnowledgeUnit(Color.YELLOW, 0, 0),
  ParameterProber(Color.RED, 10, 5),
  Abstractor(Color.RED, 50, 25),
  Reviewer(Color.MAGENTA, 0, 0);
  
  Color color;
  private final int successPoints;
  private final int failurePoints;

  private TutorType(Color color, int successPoints, int failurePoints) {
    this.color = color;
    this.successPoints = successPoints;
    this.failurePoints = failurePoints;
  }
  
  public Color getColor() {
    return color;
  }
  
  public String getLocalizedName() {
    return name();
  }

  @Override
  public int getSuccessPoints() {
    return successPoints;
  }

  @Override
  public int getFailurePoints() {
    return failurePoints;
  }
}