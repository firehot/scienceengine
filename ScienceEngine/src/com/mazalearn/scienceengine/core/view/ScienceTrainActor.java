package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class ScienceTrainActor extends Group {
  private final IScience2DView science2DView;
  private static final int NUM_ENGINE_WHEELS = 4;
  Actor coach;

  public ScienceTrainActor(Science2DBody body, IScience2DView science2DView, Skin skin) {
    super();
    setName(ComponentType.ScienceTrain.name());
    body.setActive(true);
    this.science2DView = science2DView;
    Image engine = new Image(ScienceEngine.getTextureRegion("engine"));
    addActor(engine);
    engine.setSize(180, 35);
    // Add wheels to the engine
    for (int i = 0; i < NUM_ENGINE_WHEELS; i++) {
      Image wheel = new Image(ScienceEngine.getTextureRegion("wheel"));
      wheel.setPosition(i * 40, -20);
      wheel.setSize(25, 25);
      wheel.setOrigin(wheel.getWidth()/2, wheel.getWidth()/2);
      wheel.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      addActor(wheel);
    }
  }
  
  public void draw(SpriteBatch batch, float parentAlpha) {
    CoachDrawingActor coachDrawing = (CoachDrawingActor) science2DView.findActor("Drawing");
    if (coachDrawing == null) return;
    if (coachDrawing.hasChangedSinceSnapshot()) {
      coachDrawing.takeSnapshot();
    }
    if (coach == null) {
      coach = coachDrawing.getCoach();
      coach.setPosition(-30 - coach.getWidth(), -20);
      addActor(coach);
    }
    super.draw(batch, parentAlpha);
  }
  
  public void reset() {
    clearActions();
  }

  public void animate() {
    addAction(Actions.repeat(-1, 
      Actions.sequence(
          Actions.moveTo(0,  getY()),
          Actions.moveBy(ScreenComponent.VIEWPORT_WIDTH, 0, 10), 
          Actions.moveBy(-ScreenComponent.VIEWPORT_WIDTH,0))));
  }
}