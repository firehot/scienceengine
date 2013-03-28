package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;

public class ProbeImage extends Image {
  public ProbeImage() {
    super(ScienceEngine.getTextureRegion("questionmark"));
    this.addAction(
        Actions.forever(
            Actions.sequence(
                Actions.alpha(0.5f, 1f),
                Actions.alpha(1f, 1f),
                Actions.delay(0.5f))
            )
        ); 
  }
}
