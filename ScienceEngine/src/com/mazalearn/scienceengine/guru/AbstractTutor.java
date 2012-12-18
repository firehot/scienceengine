package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public abstract class AbstractTutor extends Group implements ITutor{

  protected Array<?> components;
  private int deltaFailureScore;
  private int deltaSuccessScore;

  public AbstractTutor(int deltaSuccessScore, int deltaFailureScore) {
    this.deltaSuccessScore = deltaSuccessScore;
    this.deltaFailureScore = deltaFailureScore;
  }

  @Override
  public abstract String getTitle();

  @Override
  public abstract void activate(boolean activate);

  @Override
  public abstract void reinitialize(float x, float y, float width, float height, boolean probeMode);

  @Override
  public abstract String getHint();

  @Override
  public int getSuccessScore() {
    return deltaSuccessScore;
  }
  
  @Override
  public int getFailureScore() {
    return deltaFailureScore;
  }
  
  @Override
  public void checkProgress() {
  }
  @Override
  public void initializeComponents(Array<?> components) {
    this.components = components;
  }
  
  @Override
  public abstract boolean isCompleted();
}