package com.mazalearn.scienceengine.domains.waves;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.waves.model.Boundary;
import com.mazalearn.scienceengine.domains.waves.model.ComponentType;
import com.mazalearn.scienceengine.domains.waves.model.WaveMaker;
import com.mazalearn.scienceengine.domains.waves.model.WaveBox;
import com.mazalearn.scienceengine.domains.waves.model.WaveBox.Ball;

public class WaveModel extends AbstractScience2DModel {

  private Boundary boundary;
  private WaveMaker waveMaker;
  private WaveBox waveBox;
  private int numBalls;
  // Array of balls to simulate wave on string
  public Ball balls[];
 
  public WaveModel(int numBalls) {
    this.numBalls = numBalls;
  }

  @Override
  public void singleStep() {
    int frameCount = (int) (11 - waveBox.getTension());
    if (ScienceEngine.getLogicalTime() % frameCount == 0) {
      waveBox.singleStep(boundary.boundaryType());
      balls[0].pos.y = waveMaker.getPos(ScienceEngine.getLogicalTime(), balls[0].pos.y);
    }
  }

  @Override
  public void reset() {
    super.reset();
    waveMaker.setAmplitude(3 * WaveBox.BALL_DIAMETER);
  }

  @Override
  protected Science2DBody createScience2DBody(String componentTypeName,
      float x, float y, float rotation) {
    ComponentType componentType = null;
    try {
      componentType = ComponentType.valueOf(componentTypeName);
    } catch (IllegalArgumentException e) {
      return super.createScience2DBody(componentTypeName, x, y, rotation);
    }
    
    switch(componentType) {
    case WaveBox: 
      waveBox = new WaveBox(componentType, x, y, rotation, numBalls);
      balls = waveBox.balls;
      return waveBox;
    case Boundary: return boundary = new Boundary(componentType, x, y, rotation);
    case WaveMaker: return waveMaker = new WaveMaker(componentType, x, y, rotation);
    }
    
    return null;
  }

  @Override
  public void initializeConfigs(List<IModelConfig<?>> modelConfigs) {
  }
  
}
