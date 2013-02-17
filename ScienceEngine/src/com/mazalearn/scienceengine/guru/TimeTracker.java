package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.utils.Format;

public class TimeTracker extends Label {
  private float activeTime;
  private Guru guru;
  
  public TimeTracker(Guru guru, CharSequence text, Skin skin) {
    super(text, skin);
    this.guru = guru;
  }

  @Override
  public void act(float delta) {
    if (!guru.isVisible()) return;
    activeTime += delta;
    this.setText(Format.formatTime(activeTime));
    // Cost it to the active Tutor
    guru.getActiveTutor().addTimeSpent(delta);
  }

  public float getActiveTime() {
    return activeTime;
  }
}