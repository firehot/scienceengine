package com.mazalearn.scienceengine.domains.waves;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.domains.waves.view.Boundary;
import com.mazalearn.scienceengine.domains.waves.view.Hand;
import com.mazalearn.scienceengine.domains.waves.view.WaveBox;

public class WaveView extends AbstractScience2DView {

  private static final float ORIGIN_X = 4;
  private Hand hand;
  private Boundary boundary;
  private WaveBox waveBox;
  
  private final WaveModel waveModel;
  
  
  public WaveView(float width, float height, final WaveModel waveModel,
      Skin skin, TextureAtlas atlas, IScience2DController controller) {
    super(waveModel, width, height, skin, controller);
    this.waveModel = waveModel;
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
  public void prepareView() {
    waveBox = (WaveBox) findActor("Wavebox");
    hand = (Hand) findActor("Hand");
    boundary = (Boundary) findActor("Boundary");
    boundary.setWaveBox(waveBox);
    this.setBallDiameter(waveBox.getBallDiameter());
    super.prepareView();
  }
}
