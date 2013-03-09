package com.mazalearn.scienceengine.domains.electromagnetism.probe;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.tutor.ITutor.ITutorType;

public enum TutorType implements ITutorType {
  FieldMagnitudeProber(Color.RED),
  FieldDirectionProber(Color.RED),
  LightProber(Color.RED);
  
  Color color;

  private TutorType(Color color) {
    this.color = color;
  }
  
  public Color getColor() {
    return color;
  }
}