package com.mazalearn.scienceengine.domains.electromagnetism.tutor;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.tutor.ITutor.ITutorType;

public enum TutorType implements ITutorType {
  FieldMagnitudeProber(Color.RED, 10, 5),
  FieldDirectionProber(Color.RED, 10, 5),
  LightProber(Color.RED, 10, 5);
  
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