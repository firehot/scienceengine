package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class ScienceTrain extends Group {
  /**
   * 
   */
  private final IScience2DView science2DView;
  private static final int NUM_ENGINE_WHEELS = 4;
  Image coach, wheel1, wheel2;
  private TextureRegion lightTexture;

  public ScienceTrain(IScience2DView science2DView) {
    super();
    this.science2DView = science2DView;
    lightTexture = createLightTexture(Color.YELLOW);
    Image engine = new Image(ScienceEngine.assetManager.get("images/engine.png", Texture.class));
    addAction(Actions.repeat(-1, 
        Actions.sequence(
            Actions.moveBy(AbstractScreen.VIEWPORT_WIDTH, 0, 20), 
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
  
  private TextureRegion createLightTexture(Color color) {
    Pixmap pixmap = new Pixmap(256, 256 , Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fillCircle(256/2, 256/2, 256/2);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return textureRegion;
  }
  
  private void drawLight(SpriteBatch batch) {
    float intensity = MathUtils.sinDeg(wheel1.getRotation()/8);
    float diameter = 32 * intensity;
    float lightRadius = diameter / 2;
    Color c = batch.getColor();
    batch.setColor(1, 1, 1, 0.5f + intensity * 0.25f);
    batch.draw(lightTexture, getX() - 20 - lightRadius, 
        getY() + 35 - lightRadius, diameter, diameter);
    batch.setColor(c);
  }
  
  public void draw(SpriteBatch batch, float parentAlpha) {
    DrawingActor coachDrawing = (DrawingActor) science2DView.findActor("Drawing.1");
    if (coachDrawing != null && coachDrawing.getImage() != coach) {
      coach = coachDrawing.getImage();
      coach.setSize(128, 64);
      coach.setPosition(-coach.getWidth() - 20, -20);
      addActor(coach);
    }
    DrawingActor wheel1Drawing = (DrawingActor) science2DView.findActor("Drawing.2");
    if (wheel1Drawing != null && wheel1Drawing.getImage() != wheel1) {
      wheel1 = wheel1Drawing.getImage();
      wheel1.setSize(32, 32);
      wheel1.setPosition(- 3.5f * wheel1.getWidth() - 20, -20);
      wheel1.setOrigin(wheel1.getWidth()/2, wheel1.getHeight()/2);
      wheel1.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      addActor(wheel1);
    }
    DrawingActor wheel2Drawing = (DrawingActor) science2DView.findActor("Drawing.3");
    if (wheel2Drawing != null && wheel2Drawing.getImage() != wheel2) {
      wheel2 = wheel2Drawing.getImage();
      wheel2.setSize(32, 32);
      wheel2.setPosition(- 1.5f * wheel2.getWidth() - 20, -20);
      wheel2.setOrigin(wheel2.getWidth()/2, wheel2.getHeight()/2);
      wheel2.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      addActor(wheel2);
    }
    super.draw(batch, parentAlpha);
    if (wheel1Drawing.hasBeenDrawn() && coachDrawing.hasBeenDrawn() && wheel2Drawing.hasBeenDrawn()) {
      drawLight(batch);
    }
  }
}