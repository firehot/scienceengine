package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;

public class Hinter extends Group {
  private static final int TIME_GAP_BETWEEN_HINTS = 5;
  private String hint;
  private TextButton hintBox;
  private ITutor activeTutor;
  private float lastTimeHintShown = -TIME_GAP_BETWEEN_HINTS;
  
  public Hinter(Skin skin) {
    hintBox = new TextButton("", skin, "default-small");
    this.addActor(hintBox);
    hintBox.setSize(ScreenComponent.Hint.getWidth(), ScreenComponent.Hint.getHeight());
    hintBox.setPosition(ScreenComponent.Hint.getX(), ScreenComponent.Hint.getY());
    hintBox.getLabel().setWrap(true);
    this.setVisible(false);
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (ScienceEngine.getTime() - lastTimeHintShown < TIME_GAP_BETWEEN_HINTS) return;
    if (activeTutor != null) {
      setHint(activeTutor.getHint());
    }
  }
  
  public void setActiveTutor(ITutor activeTutor) {
    this.activeTutor = activeTutor;
    clearHint();
  }
  
  public void setHint(final String hint) {
    if (hint == this.hint) return;
    if (hint == null) {
      this.setVisible(false);
      return;
    }
    this.hint = hint;
    this.setVisible(true);
    lastTimeHintShown = ScienceEngine.getTime();
    hintBox.addAction(Actions.sequence(
        Actions.fadeOut(0.5f),
        new Action() {
          @Override
          public boolean act(float delta) {
            hintBox.setText(hint);
            return true;
          }
        },
        Actions.fadeIn(0.5f)));
        
  }

  public boolean hasHint() {
    return hint != null;
  }
  
  public void clearHint() {
    this.hint = null;
    if (getStage() != null) {
      hintBox.setText("");
      lastTimeHintShown = -TIME_GAP_BETWEEN_HINTS;
      this.setVisible(false);
    }
  }

}
