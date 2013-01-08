package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Lightbulb;

public class ScienceTrain extends Group {
  /**
   * 
   */
  private final IScience2DView science2DView;
  private static final int NUM_ENGINE_WHEELS = 4;
  Image coach, wheel1, wheel2, bulb;

  public ScienceTrain(IScience2DView science2DView) {
    super();
    this.science2DView = science2DView;
    Image engine = new Image(ScienceEngine.assetManager.get("images/engine.png", Texture.class));
    addAction(Actions.repeat(-1, 
        Actions.sequence(
            Actions.moveBy(AbstractScreen.VIEWPORT_WIDTH, 0, 10), 
            Actions.moveBy(-AbstractScreen.VIEWPORT_WIDTH,0))));
    addActor(engine);
    engine.setSize(180, 35);
    setPosition(0, 0);
    // Add wheels to the engine
    for (int i = 0; i < NUM_ENGINE_WHEELS; i++) {
      Image wheel = new Image(ScienceEngine.assetManager.get("images/wheel.png", Texture.class));
      wheel.setPosition(i * 40, -20);
      wheel.setSize(25, 25);
      wheel.setOrigin(wheel.getWidth()/2, wheel.getWidth()/2);
      wheel.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      addActor(wheel);
    }
  }
  
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    DrawingActor drawing = (DrawingActor) science2DView.findActor("Drawing.2");
    if (drawing != null && drawing.getImage() != wheel1) {
      wheel1 = drawing.getImage();
      wheel1.setSize(25, 25);
      wheel1.setPosition(- 3.5f * wheel1.getWidth() - 20, -20);
      wheel1.setOrigin(wheel1.getWidth()/2, wheel1.getHeight()/2);
      wheel1.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      addActor(wheel1);
    }
    drawing = (DrawingActor) science2DView.findActor("Drawing.3");
    if (drawing != null && drawing.getImage() != wheel2) {
      wheel2 = drawing.getImage();
      wheel2.setSize(25, 25);
      wheel2.setPosition(- 1.5f * wheel2.getWidth() - 20, -20);
      wheel2.setOrigin(wheel2.getWidth()/2, wheel2.getHeight()/2);
      wheel2.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      addActor(wheel2);
    }
    drawing = (DrawingActor) science2DView.findActor("Drawing.1");
    if (drawing != null && drawing.getImage() != coach) {
      coach = drawing.getImage();
      coach.setSize(100, 60);
      coach.setPosition(-coach.getWidth() - 20, wheel1.getHeight() - 20);
      addActor(coach);
    }
    drawing = (DrawingActor) science2DView.findActor("Drawing.4");
    if (drawing != null && drawing.getImage() != bulb) {
      bulb = drawing.getImage();
      bulb.setSize(25, 25);
      bulb.setPosition(- coach.getWidth()/2 - 20, wheel1.getHeight() + 40);
      addActor(bulb);
    }
  }
}