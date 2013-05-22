package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

/**
 * @author Mats Svensson
 */
public class LoadingScienceTrain extends AbstractScreen {

  private static final int NUM_ENGINE_WHEELS = 4;
  private static final int WIDTH = 450;

  private float startX, endX;
  private float percent;

  private AbstractScreen nextScreen;
  private Group train;
  private float startY;
  private float endY;

  public LoadingScienceTrain(ScienceEngine scienceEngine, AbstractScreen nextScreen) {
    super(scienceEngine);
    this.nextScreen = nextScreen;
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(true);
    }
    nextScreen.addAssets();
  }

  @Override
  protected boolean needsLayout() {
    return false;
  }

  @Override
  public void show() {
    super.show();

    Label loading = new Label("Loading...", getSkin(), "default-big");
    loading.setColor(Color.ORANGE);
    loading.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - loading.getWidth() / 2, ScreenComponent.VIEWPORT_HEIGHT - 30);
    stage.addActor(loading);

    train = new Group();
    Image engine = new Image(ScienceEngine.getTextureRegion("engine"));
    train.addActor(engine);
    engine.setSize(180, 35);
    // Add wheels to the engine
    for (int i = 0; i < NUM_ENGINE_WHEELS; i++) {
      Image wheel = new Image(ScienceEngine.getTextureRegion("wheel"));
      wheel.setPosition(i * 40, -20);
      wheel.setSize(25, 25);
      wheel.setOrigin(wheel.getWidth()/2, wheel.getWidth()/2);
      wheel.addAction(Actions.repeat(-1, Actions.rotateBy(-360, 1)));
      train.addActor(wheel);
    }
    stage.addActor(train);
  }

  @Override
  public void resize(int width, int height) {
    // Set our screen to always be XXX x 480 in size
    width = ScreenComponent.VIEWPORT_HEIGHT * width / height;
    height = ScreenComponent.VIEWPORT_HEIGHT;
    stage.setViewport(width, height, false);
    startX = 50;
    endX = WIDTH - 10;
    startY = ScreenComponent.VIEWPORT_HEIGHT * MathUtils.random(0.2f, 0.8f);
    endY = ScreenComponent.VIEWPORT_HEIGHT * MathUtils.random(0.2f, 0.8f);
    train.setPosition(startX, startY);
    train.setRotation(MathUtils.atan2(endY - startY, endX - startX) * MathUtils.radiansToDegrees);

  }

  @Override
  public void render(float delta) {
    // Clear the screen
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

    // Load some, will return true if done loading
    if (ScienceEngine.getAssetManager().update()) {
      scienceEngine.setScreen(nextScreen);
    }

    // Interpolate the percentage to make it smoother
    percent = Interpolation.linear.apply(percent,
        ScienceEngine.getAssetManager().getProgress(), 0.1f);

    // Update positions (and size) to match the percentage
    train.setX(startX + endX * percent);
    train.setY(startY + (endY - startY) * percent);

    // Show the loading screen
    stage.act();
    stage.draw();
  }

  @Override
  public void hide() {
    super.hide();
  }

  @Override
  protected void goBack() {
    // Ignore.
  }
}