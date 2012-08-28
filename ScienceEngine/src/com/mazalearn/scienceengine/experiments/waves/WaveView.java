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
  private TextureRegion ballTexture;
  private Texture backgroundTexture;
  private final WaveModel waveModel;
  private final int numBalls;
  private final int ballDiameter;
  
  public WaveView(float width, float height, final WaveModel waveModel, 
      int numBalls, int ballDiameter, TextureAtlas atlas) {
    super(waveModel);
    this.width = width;
    this.height = height;
    this.waveModel = waveModel;
    this.numBalls = numBalls;
    this.ballDiameter = ballDiameter;
    this.ORIGIN_X = ballDiameter;
    this.ORIGIN_Y = 10 * ballDiameter;
    
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

    startBall = new Image(handRegion, Scaling.stretch) {
      float downY;
      public boolean touchDown(float x, float y, int pointer) {
        downY = y;
        return true;
      }
      public void touchDragged(float x, float y, int pointer) {
        waveModel.balls[0].pos.y += y - downY;
        resume();
      }
    };
    startBall.width *= 4; startBall.height *= 4;
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
      waveModel.simulateSteps(1);
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
}
