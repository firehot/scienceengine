package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;

public class ScienceTrainActor extends Group {
  private final IScience2DView science2DView;
  private static final int NUM_ENGINE_WHEELS = 4;
  Actor coach;
  private TextureRegion lightTexture;
  private boolean drawLight;

  public ScienceTrainActor(Science2DBody body, IScience2DView science2DView, Skin skin) {
    super();
    setName(ComponentType.ScienceTrain.name());
    body.setActive(true);
    this.science2DView = science2DView;
    lightTexture = createLightTexture(Color.YELLOW);
    Image engine = new Image(ScienceEngine.assetManager.get("images/engine.png", Texture.class));
    addActor(engine);
    engine.setSize(180, 35);
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
  
  private TextureRegion createLightTexture(Color color) {
    Pixmap pixmap = new Pixmap(256, 256 , Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fillCircle(256/2, 256/2, 256/2);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return textureRegion;
  }
  
  private void drawLight(SpriteBatch batch) {
    float intensity = MathUtils.sinDeg(getX());
    float diameter = 32 * intensity;
    float lightRadius = diameter / 2;
    Color c = batch.getColor();
    batch.setColor(1, 1, 1, 0.5f + intensity * 0.25f);
    batch.draw(lightTexture, getX() - 40 - lightRadius, 
        getY() + 15 - lightRadius, diameter, diameter);
    batch.setColor(c);
  }
  
  public void draw(SpriteBatch batch, float parentAlpha) {
    DrawingActor coachDrawing = (DrawingActor) science2DView.findActor("Drawing");
    if (coachDrawing == null) return;
    if (coachDrawing.hasChangedSinceSnapshot()) {
      coachDrawing.takeSnapshot();
      if (coach == null) {
        coach = coachDrawing.getCoach();
        coach.setPosition(-30 - coach.getWidth(), -20);
        addActor(coach);
      }
    }
    super.draw(batch, parentAlpha);
    if (drawLight) {
      drawLight(batch);
    }
  }

  public void setDrawLight(boolean drawLight) {
    this.drawLight = drawLight;
    if (drawLight) {
      setPosition(0, getY());
      addAction(Actions.repeat(-1, 
        Actions.sequence(
            Actions.moveBy(AbstractScreen.VIEWPORT_WIDTH, 0, 20), 
            Actions.moveBy(-AbstractScreen.VIEWPORT_WIDTH,0))));
    } else {
      this.clearActions();
    }    
  }
}