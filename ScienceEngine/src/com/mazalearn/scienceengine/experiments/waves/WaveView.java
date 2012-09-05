package com.mazalearn.scienceengine.experiments.waves;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.experiments.waves.view.Boundary;
import com.mazalearn.scienceengine.experiments.waves.view.Hand;
import com.mazalearn.scienceengine.experiments.waves.view.WaveBox;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

public class WaveView extends AbstractExperimentView {

  private Actor hand, boundary, waveBox;
  
  private final float ORIGIN_Y;
  private final float ORIGIN_X;
  private TextureRegion ballTextureRed;
  private Texture backgroundTexture;
  private final WaveModel waveModel;
  private final int ballDiameter;

  private TextureRegion ballTextureBlue;
  
  public WaveView(float width, float height, final WaveModel waveModel, 
      int ballDiameter, TextureAtlas atlas) {
    super(waveModel);
    this.width = width;
    this.height = height;
    this.waveModel = waveModel;
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
    
    waveBox = new WaveBox(ballTextureRed, ballTextureBlue, backgroundTexture, 
        waveModel.balls, this.x + ORIGIN_X, this.y + ORIGIN_Y);
    hand = new Hand(atlas.findRegion("wave-view/hand-pointer"), 
        Scaling.stretch, waveModel.balls[0], 
        this.x + ORIGIN_X - ballDiameter, this.y + ORIGIN_Y);
    boundary = new Boundary(ballTextureRed, 
        waveModel.balls[waveModel.balls.length - 1], 
        this.x + ORIGIN_X + ballDiameter, this.y + ORIGIN_Y);
    addActor(waveBox);
    addActor(hand);
    addActor(boundary);
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
    // Advance n steps
    if (!isPaused ) {
      waveModel.simulateSteps(1);
    }
    hand.visible = waveModel.getGenMode() == "Manual";
    super.draw(batch, parentAlpha);
  }
}
