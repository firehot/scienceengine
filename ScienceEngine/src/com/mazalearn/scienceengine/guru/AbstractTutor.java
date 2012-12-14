package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public abstract class AbstractTutor extends Group implements ITutor{

  protected Array<?> components;

  @Override
  public abstract String getTitle();

  @Override
  public abstract void activate(boolean activate);

  @Override
  public abstract void reinitialize(float x, float y, float width, float height, boolean probeMode);

  @Override
  public abstract String getHint();

  @Override
  public abstract int getDeltaSuccessScore();

  @Override
  public abstract int getDeltaFailureScore();

  @Override
  public abstract void checkProgress();

  @Override
  public void initializeComponents(Array<?> components) {
    this.components = components;
  }
  
  @Override
  public abstract boolean isCompleted();
}