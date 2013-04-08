package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.tutor.ITutor.ITutorType;

public enum TutorType implements ITutorType {
  Root(Color.CLEAR), 
  Guide(Color.YELLOW), 
  Challenge(Color.RED), 
  RapidFire(Color.MAGENTA),
  MCQ1(Color.MAGENTA),
  MCQ(Color.MAGENTA),
  KnowledgeUnit(Color.YELLOW),
  ParameterProber(Color.RED),
  Abstractor(Color.RED),
  Reviewer(Color.MAGENTA);
  
  Color color;

  private TutorType(Color color) {
    this.color = color;
  }
  
  public Color getColor() {
    return color;
  }
  
  public String getLocalizedName() {
    return name();
  }
}