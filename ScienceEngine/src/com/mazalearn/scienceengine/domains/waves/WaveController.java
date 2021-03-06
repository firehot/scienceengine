package com.mazalearn.scienceengine.domains.waves;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.core.controller.AbstractScience2DController;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.waves.model.ComponentType;
import com.mazalearn.scienceengine.domains.waves.view.BoundaryActor;
import com.mazalearn.scienceengine.domains.waves.view.WaveBoxActor;
import com.mazalearn.scienceengine.domains.waves.view.WaveMakerActor;

/**
 * Wave Motion science2DModel
 */
public class WaveController extends AbstractScience2DController {
  
  private static final int NUM_BALLS = 40;
  private static final float ORIGIN_Y = 10;
  private static final float ORIGIN_X = 4;
  private WaveModel waveModel;
  private TextureRegion ballTextureRed;
  private TextureRegion ballTextureBlue;
  private WaveView waveView;

  public WaveController(Topic level, int width, int height, Skin skin) {
    super(Topic.Waves, level, skin);
    this.waveModel = new WaveModel(NUM_BALLS);;
    waveView = new WaveView(width, height, waveModel, skin, this);
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
    ComponentType componentType;
    try {
      componentType = ComponentType.valueOf(type);
    } catch(IllegalArgumentException e) {
      return super.createActor(type, viewSpec, body);
    }
    switch (componentType) {
    case WaveBox: 
      return new WaveBoxActor(body, ballTextureRed, ballTextureBlue, 
          ORIGIN_X, ORIGIN_Y, waveView);
    case Boundary:
      return new BoundaryActor(body, ballTextureRed, 
          waveModel.balls[waveModel.balls.length - 1], ORIGIN_Y);
    case WaveMaker:
      return new WaveMakerActor(body, waveModel.balls[0], ORIGIN_X - 1, ORIGIN_Y);
    }
    return null;
  }  

}
