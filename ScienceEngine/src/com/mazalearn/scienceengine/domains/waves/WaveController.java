package com.mazalearn.scienceengine.domains.waves;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.AbstractScience2DController;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.waves.view.Boundary;
import com.mazalearn.scienceengine.domains.waves.view.Hand;
import com.mazalearn.scienceengine.domains.waves.view.WaveBox;

/**
 * Wave Motion science2DModel
 */
public class WaveController extends AbstractScience2DController {
  
  public static final String DOMAIN = "Waves";
  private static final int NUM_BALLS = 40;
  private static final float ORIGIN_Y = 10;
  private static final float ORIGIN_X = 4;
  private WaveModel waveModel;
  private TextureRegion ballTextureRed;
  private TextureRegion ballTextureBlue;
  private WaveView waveView;

  public WaveController(int level, int width, int height, TextureAtlas atlas, Skin skin) {
    super(DOMAIN, level, skin);
    this.waveModel = new WaveModel(NUM_BALLS);;
    waveView = new WaveView(width, height, waveModel, skin, atlas, this);
    initialize(waveModel, waveView);
    
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
  protected Actor createActor(String type, String viewSpec, Science2DBody body) {
    if (type.equals("WaveBox")) {
      return new WaveBox(ballTextureRed, ballTextureBlue, 
          waveModel.balls, ORIGIN_X, ORIGIN_Y, waveView);
    } else if (type.equals("Boundary")) {
      return new Boundary(ballTextureRed, 
          waveModel.balls[waveModel.balls.length - 1], ORIGIN_Y);
    } else if (type.equals("Hand")) {
      return new Hand(waveModel.balls[0], ORIGIN_X - 1, ORIGIN_Y);
    }
    return null;
  }  

}
