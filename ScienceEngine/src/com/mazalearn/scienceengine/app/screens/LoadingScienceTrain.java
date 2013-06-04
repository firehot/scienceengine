package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;

/**
 * @author Mats Svensson
 */
public class LoadingScienceTrain extends AbstractScreen {

  private float startX, endX;
  private float percent;

  private AbstractScreen nextScreen;
  private Actor train;
  private float startY;
  private float endY;
  private Image railTracks;
  private Label loading;

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

    loading = new Label("Loading...Please Wait...", getSkin(), "default-big");
    loading.setColor(Color.ORANGE);
    loading.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - loading.getWidth() / 2,
        ScreenComponent.VIEWPORT_HEIGHT - 30);
    stage.addActor(loading);

    railTracks = new Image(ScienceEngine.getTextureRegion("railtracks"));
    ScreenComponent.scaleSize(railTracks, railTracks.getWidth() * 3, railTracks.getHeight() * 0.35f);
    stage.addActor(railTracks);

    train = ScreenUtils.createScienceTrain(25);
    stage.addActor(train);
  }

  @Override
  public void resize(int width, int height) {
    // Set our screen to always be XXX x 480 in size
    width = ScreenComponent.VIEWPORT_HEIGHT * width / height;
    height = ScreenComponent.VIEWPORT_HEIGHT;
    stage.setViewport(width, height, false);
    startX = 50;
    endX = ScreenComponent.VIEWPORT_WIDTH - 10;
    startY = ScreenComponent.VIEWPORT_HEIGHT * MathUtils.random(0.2f, 0.8f);
    endY = ScreenComponent.VIEWPORT_HEIGHT * MathUtils.random(0.2f, 0.8f);
    train.setPosition(startX, startY);
    float angle = MathUtils.atan2(endY - startY, endX - startX) * MathUtils.radiansToDegrees;
    train.setRotation(angle);
    railTracks.setPosition(startX - ScreenComponent.getScaledX(100 * MathUtils.cosDeg(angle)), 
        startY - ScreenComponent.getScaledY(25 + 100 * MathUtils.sinDeg(angle)));
    railTracks.setRotation(angle);
  }

  @Override
  public void render(float delta) {
    // Clear the screen
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

    // Load some, will return true if done loading
    AssetManager assetManager = ScienceEngine.getAssetManager();
    
    if (assetManager.update()) {
      scienceEngine.setScreen(nextScreen);
    }
    // Interpolate the percentage to make it smoother
    percent = Interpolation.linear.apply(percent,
        assetManager.getProgress(), 0.1f);

    loading.setText("Loading...Please Wait..." + Math.round(percent*100) + "%");

    // Update positions (and size) to match the percentage
    train.setX(startX + endX * percent);
    train.setY(startY + (endY - startY) * percent);
    delayIfDebug();

    // Show the loading screen
    stage.act();
    stage.draw();
  }

  private void delayIfDebug() {
    if (!ScienceEngine.DEV_MODE.isDebug()) return;
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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