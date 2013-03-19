package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SizeToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/**
 * Workaround for SizeTo action not working properly.
 * It does not do an invalidate of the actor.
 * @author sridhar
 *
 */
public class SizeAction extends SizeToAction {

  protected void update (float percent) {
    super.update(percent);
    ((Widget) actor).invalidate();
  }

  static public SizeAction sizeTo(float x, float y, float duration) {
    SizeAction action = Actions.action(SizeAction.class);
    action.setSize(x, y);
    action.setDuration(duration);
    action.setInterpolation(null);
    return action;
  }
}
