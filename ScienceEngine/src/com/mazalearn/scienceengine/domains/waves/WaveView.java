package com.mazalearn.scienceengine.domains.waves;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.domains.waves.model.ComponentType;
import com.mazalearn.scienceengine.domains.waves.view.BoundaryActor;
import com.mazalearn.scienceengine.domains.waves.view.WaveMakerActor;
import com.mazalearn.scienceengine.domains.waves.view.WaveBoxActor;

public class WaveView extends AbstractScience2DView {

  private static final float ORIGIN_X = 4;
  private WaveMakerActor waveMakerActor;
  private BoundaryActor boundaryActor;
  private WaveBoxActor waveBoxActor;
  
  private final WaveModel waveModel;
  
  
  public WaveView(float width, float height, final WaveModel waveModel,
      Skin skin, TextureAtlas atlas, IScience2DController controller) {
    super(waveModel, width, height, skin, controller);
    this.waveModel = waveModel;
  }
  
  public void setBallDiameter(int ballDiameter) {
    if (waveMakerActor == null || boundaryActor == null) return;
    waveMakerActor.setBallDiameter(ballDiameter);
    waveMakerActor.setX(waveBoxActor.getX() + (ORIGIN_X - 4 + waveModel.balls[0].pos.x) * ballDiameter);
    boundaryActor.setBallDiameter(ballDiameter);
    boundaryActor.setX(waveBoxActor.getX() + (ORIGIN_X + 1 + waveModel.balls[waveModel.balls.length - 1].pos.x) * ballDiameter);
  }

  @Override
  public void prepareView() {
    waveBoxActor = (WaveBoxActor) findActor(ComponentType.WaveBox.name());
    waveMakerActor = (WaveMakerActor) findActor(ComponentType.WaveMaker.name());
    boundaryActor = (BoundaryActor) findActor(ComponentType.Boundary.name());
    boundaryActor.setWaveBox(waveBoxActor);
    this.setBallDiameter(waveBoxActor.getBallDiameter());
    super.prepareView();
  }
}
