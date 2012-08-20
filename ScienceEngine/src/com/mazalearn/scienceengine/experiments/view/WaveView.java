package com.mazalearn.scienceengine.experiments.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.experiments.model.WaveModel;
import com.mazalearn.scienceengine.experiments.model.WaveModel.Ball;

public class WaveView extends Group implements IExperimentView {
  private Actor startBall, endBall;
  
  private static final float ORIGIN_Y = 80f;
  private static final float ORIGIN_X = 1f;
  private TextureRegion ballTexture;
  private Texture backgroundTexture;
  private boolean isPaused = false;
  
  private final WaveModel waveModel;
  private final int numBalls;
  private final int ballDiameter;
  
  public WaveView(float width, float height, final WaveModel waveModel, 
      int numBalls, int ballDiameter, TextureAtlas atlas) {
    this.width = width;
    this.height = height;
    this.waveModel = waveModel;
    this.numBalls = numBalls;
    this.ballDiameter = ballDiameter;
    
    ballTexture = createBallTexture();
    // Use light-gray background color
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0, 0, 1, 1);
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    
    // retrieve the splash image's region from the atlas
    AtlasRegion handRegion = atlas.findRegion(
        "wave-view/hand-pointer");

    // here we create the splash image actor; its size is set when the
    // resize() method gets called
    startBall = new Image(handRegion, Scaling.stretch) {
      public boolean touchDown(float x, float y, int pointer) {
        return true;
      }
      public void touchDragged(float x, float y, int pointer) {
        waveModel.balls[0].pos.y += y;
        resume();
      }
    };
    endBall = new Image(ballTexture);
    startBall.x = ORIGIN_X + waveModel.balls[0].pos.x - ballDiameter;
    endBall.x = ORIGIN_X + waveModel.balls[numBalls - 1].pos.x;
    addActor(startBall);
    addActor(endBall);
  }

  private TextureRegion createBallTexture() {
    // Create texture region for ball
    Pixmap pixmap = new Pixmap(ballDiameter, ballDiameter, Format.RGBA8888);
    pixmap.setColor(Color.RED);
    pixmap.fillCircle(ballDiameter/2, ballDiameter/2, ballDiameter/2);
    TextureRegion ballTexture = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return ballTexture;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw background
    batch.draw(backgroundTexture, this.x, this.y, this.width, this.height);
    // Advance n steps
    if (!isPaused ) {
      waveModel.singleStep();
    }
    startBall.y = ORIGIN_Y + waveModel.balls[0].pos.y;
    endBall.y = ORIGIN_Y + waveModel.balls[numBalls - 1].pos.y;
    startBall.visible = waveModel.getGenMode() == "Manual";
    // Draw the molecules
    for (Ball ball: waveModel.balls) {
      batch.draw(ballTexture, this.x + ORIGIN_X + ball.pos.x, this.y + ORIGIN_Y + ball.pos.y);
    }
    super.draw(batch, parentAlpha);
  }

  @Override
  public void pause() {
    this.isPaused = true;
  }

  @Override
  public void resume() {
    this.isPaused = false;
  }

  @Override
  public boolean isPaused() {
    return isPaused;
  }
}
