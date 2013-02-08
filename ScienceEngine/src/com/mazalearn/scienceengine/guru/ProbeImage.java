package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class ProbeImage extends Image {
  private static Texture QUESTION_MARK = new Texture("images/questionmark.png");
  
  public ProbeImage() {
    super(QUESTION_MARK);
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
