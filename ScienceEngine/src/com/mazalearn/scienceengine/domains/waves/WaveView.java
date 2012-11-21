package com.mazalearn.scienceengine.domains.waves;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractScience2DStage;
import com.mazalearn.scienceengine.domains.waves.view.Boundary;
import com.mazalearn.scienceengine.domains.waves.view.Hand;
import com.mazalearn.scienceengine.domains.waves.view.WaveBox;

public class WaveView extends AbstractScience2DStage {

  private Hand hand;
  private Boundary boundary;
  private WaveBox waveBox;
  
  private final float ORIGIN_Y;
  private final float ORIGIN_X;
  private TextureRegion ballTextureRed;
  private final WaveModel waveModel;
  
  private TextureRegion ballTextureBlue;
  
  public WaveView(float width, float height, final WaveModel waveModel,
      Skin skin, TextureAtlas atlas) {
    super(waveModel, width, height, skin);
    this.waveModel = waveModel;
    this.ORIGIN_X = 4;
    this.ORIGIN_Y = 10;
    
    ballTextureRed = createBallTexture(Color.RED);
    ballTextureBlue = createBallTexture(Color.BLUE);
  }
  
  private TextureRegion createBallTexture(Color color) {
    // Create texture region for ball
    int ballDiameter = 8;
    Pixmap pixmap = new Pixmap(ballDiameter, ballDiameter, Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fillCircle(ballDiameter/2, ballDiameter/2, ballDiameter/2);
    TextureRegion ballTexture = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return ballTexture;
  }
  
  @Override
  public void act(float delta) {
    hand.setVisible(waveModel.getGenMode() == "Manual");
    super.act(delta);
  }

  public void setBallDiameter(int ballDiameter) {
    if (hand == null || boundary == null) return;
    hand.setBallDiameter(ballDiameter);
    hand.setX(waveBox.getX() + (ORIGIN_X - 4 + waveModel.balls[0].pos.x) * ballDiameter);
    boundary.setBallDiameter(ballDiameter);
    boundary.setX(waveBox.getX() + (ORIGIN_X + 1 + waveModel.balls[waveModel.balls.length - 1].pos.x) * ballDiameter);
  }

  @Override
  protected Actor createActor(Science2DBody body) {
    return null;
  }

  @Override
  protected Actor createActor(String type) {
    if (type.equals("WaveBox")) {
      return new WaveBox(ballTextureRed, ballTextureBlue, 
          waveModel.balls, ORIGIN_X, ORIGIN_Y, this);
    } else if (type.equals("Boundary")) {
      return new Boundary(ballTextureRed, 
          waveModel.balls[waveModel.balls.length - 1], ORIGIN_Y);
    } else if (type.equals("Hand")) {
      return new Hand(waveModel.balls[0], ORIGIN_X - 1, ORIGIN_Y);
    }
    return null;
  }  

  @Override
  public void prepareStage() {
    waveBox = (WaveBox) findActor("WaveBox");
    hand = (Hand) findActor("Hand");
    boundary = (Boundary) findActor("Boundary");
    boundary.setWaveBox(waveBox);
    this.setBallDiameter(waveBox.getBallDiameter());
    super.prepareStage();
  }
}
