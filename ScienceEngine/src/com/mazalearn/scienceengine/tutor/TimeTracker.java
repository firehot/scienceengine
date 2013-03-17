package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.utils.Format;

public class TimeTracker extends Label {
  private float activeTime;
  private ITutor activeTutor;
  
  public TimeTracker(CharSequence text, Skin skin) {
    super(text, skin);
    this.setName(ScreenComponent.TimeTracker.name());
    this.setPosition(ScreenComponent.TimeTracker.getX(getWidth()),
        ScreenComponent.TimeTracker.getY(getHeight()));
  }
  
  public void setActiveTutor(ITutor activeTutor) {
    this.activeTutor = activeTutor;
  }

  @Override
  public void act(float delta) {
    if (!isVisible()) return;
    activeTime += delta;
    this.setText(Format.formatTime(activeTime));
    // Cost it to the active Tutor
    activeTutor.addTimeSpent(delta);
  }

  public float getActiveTime() {
    return activeTime;
  }
}