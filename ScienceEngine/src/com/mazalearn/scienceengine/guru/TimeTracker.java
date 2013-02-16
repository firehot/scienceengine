package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

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
    String seconds = String.valueOf(Math.round(activeTime) % 60);
    this.setText(Math.round(activeTime) / 60 + ":" + "0".substring(0, 2 - seconds.length()) + seconds);
    // Cost it to the active Tutor
    guru.getActiveTutor().addTimeSpent(delta);
  }

  public float getActiveTime() {
    return activeTime;
  }
}