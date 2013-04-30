package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

/**
 * Animate an object to call it out
 * @author sridhar
 *
 */
public class AnimateAction {

  static public Action animateSize(final float w, final float h, final boolean visible) {
    float duration = 1f;
    return Actions.forever(
        Actions.sequence(
            SizeAction.sizeTo(1.1f * w, 1.1f * h, duration),
            SizeAction.sizeTo(w, h, duration),
            new Action() { // Resets size back when animations are cleared
              @Override
              public void setActor(Actor actor) {
                if (actor == null) {
                  this.actor.setSize(w, h);
                  this.actor.setVisible(visible);
                } else {
                  actor.setVisible(true);
                }
                super.setActor(actor);
              }
              @Override
              public boolean act(float delta) {
                return true;
              }
            }
        ));
  }

  static public Action animatePosition(final float x, final float y, final boolean visible) {
    float duration = 1f;
    return Actions.forever(
        Actions.sequence(
            Actions.moveTo(x - 5, y - 5, duration),
            Actions.moveTo(x + 5, y + 5, duration),
            new Action() { // Resets position back when animations are cleared
              @Override
              public void setActor(Actor actor) {
                if (actor == null) {
                  this.actor.setPosition(x, y);
                  this.actor.setVisible(visible);
                } else {
                  actor.setVisible(true);
                }
                super.setActor(actor);
              }
              @Override
              public boolean act(float delta) {
                return true;
              }
            }
        ));
  }
}
