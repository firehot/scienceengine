package com.mazalearn.scienceengine.experiments.waves;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.core.view.AbstractExperimentView;
import com.mazalearn.scienceengine.experiments.waves.view.Boundary;
import com.mazalearn.scienceengine.experiments.waves.view.Hand;
import com.mazalearn.scienceengine.experiments.waves.view.WaveBox;

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
      Skin skin, TextureAtlas atlas) {
    super(waveModel, width, height, skin);
    this.width = width;
    this.height = height;
    this.waveModel = waveModel;
    this.ballDiameter = (int) (width / (waveModel.balls.length + 5));
    this.ORIGIN_X = 2;
    this.ORIGIN_Y = 10;
    
    ballTextureRed = createBallTexture(Color.RED);
    ballTextureBlue = createBallTexture(Color.BLUE);
    // Use light-gray background color
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0, 0, 1, 1);
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    
    waveBox = new WaveBox(ballTextureRed, ballTextureBlue, backgroundTexture, 
        waveModel.balls, ORIGIN_X, ORIGIN_Y, ballDiameter);
    hand = new Hand(atlas.findRegion("wave-view/hand-pointer"), 
        Scaling.stretch, waveModel.balls[0], 
        ORIGIN_X - 1, ORIGIN_Y, ballDiameter);
    boundary = new Boundary(ballTextureRed, 
        waveModel.balls[waveModel.balls.length - 1], 
        ORIGIN_X + 1, ORIGIN_Y, ballDiameter);
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
  public void draw() {
    hand.visible = waveModel.getGenMode() == "Manual";
    super.draw();
  }
}
