package com.mazalearn.scienceengine.experiments.waves;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.experiments.waves.WaveModel.Ball;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

public class WaveView extends AbstractExperimentView {
  private Actor startBall, endBall;
  
  private final float ORIGIN_Y;
  private final float ORIGIN_X;
  private TextureRegion ballTextureRed;
  private Texture backgroundTexture;
  private final WaveModel waveModel;
  private final int numBalls;
  private final int ballDiameter;

  private TextureRegion ballTextureBlue;
  
  public WaveView(float width, float height, final WaveModel waveModel, 
      int numBalls, int ballDiameter, TextureAtlas atlas) {
    super(waveModel);
    this.width = width;
    this.height = height;
    this.waveModel = waveModel;
    this.numBalls = numBalls;
    this.ballDiameter = ballDiameter;
    this.ORIGIN_X = 2 * ballDiameter;
    this.ORIGIN_Y = 10 * ballDiameter;
    
    ballTextureRed = createBallTexture(Color.RED);
    ballTextureBlue = createBallTexture(Color.BLUE);
    // Use light-gray background color
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0, 0, 1, 1);
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    
    // retrieve the splash image's region from the atlas
    AtlasRegion handRegion = atlas.findRegion(
        "wave-view/hand-pointer");

    startBall = new Image(handRegion, Scaling.stretch) {
      float prevY;
      public boolean touchDown(float x, float y, int pointer) {
        prevY = y;
        return true;
      }
      public void touchDragged(float x, float y, int pointer) {
        waveModel.balls[0].pos.y += y - prevY;
        prevY = y;
        System.out.println("Y = " + y);
        resume();
      }
    };
    startBall.width *= 4; startBall.height *= 4;
    endBall = new Image(ballTextureRed);
    startBall.x = this.x + ORIGIN_X + waveModel.balls[0].pos.x - ballDiameter;
    endBall.x = this.x + ORIGIN_X + waveModel.balls[numBalls - 1].pos.x + ballDiameter;
    addActor(startBall);
    addActor(endBall);
  }

  private TextureRegion createBallTexture(Color color) {
    // Create texture region for ball
    Pixmap pixmap = new Pixmap(ballDiameter, ballDiameter, Format.RGBA8888);
    pixmap.setColor(color);
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
      waveModel.simulateSteps(1);
    }
    startBall.y = this.y + ORIGIN_Y + waveModel.balls[0].pos.y;
    endBall.y = this.y + ORIGIN_Y + waveModel.balls[numBalls - 1].pos.y;
    startBall.visible = waveModel.getGenMode() == "Manual";
    // Draw the molecules
    int i = 1;
    for (Ball ball: waveModel.balls) {
      i = (i + 1) % 10;
      batch.draw(i == 0 ? ballTextureBlue : ballTextureRed, 
          this.x + ORIGIN_X + ball.pos.x, this.y + ORIGIN_Y + ball.pos.y);
    }
    super.draw(batch, parentAlpha);
  }
}
