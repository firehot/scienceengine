package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile;

public class TimeTracker extends Label {
  private float activeTime;
  private Guru guru;
  private Profile profile;

  public TimeTracker(Guru guru, CharSequence text, Skin skin) {
    super(text, skin);
    this.guru = guru;
    this.profile = ScienceEngine.getPreferencesManager().getProfile();
  }

  @Override
  public void act(float delta) {
    if (!guru.isVisible()) return;
    activeTime += delta;
    String seconds = String.valueOf(Math.round(activeTime % 60));
    this.setText(Math.round(activeTime / 60) + ":" + "0".substring(0, 2 - seconds.length()) + seconds);
    // Cost it to the active Tutor
    String subgoalId = guru.getActiveTutor().getId();
    profile.addTimeSpent(subgoalId, delta);
  }

  public float getActiveTime() {
    return activeTime;
  }
}