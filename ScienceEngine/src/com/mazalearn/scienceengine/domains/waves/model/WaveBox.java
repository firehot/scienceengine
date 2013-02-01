package com.mazalearn.scienceengine.domains.waves.model;

import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.waves.model.Boundary.BoundaryType;

public class WaveBox extends Science2DBody {

  // Ball is a segment of the LIST used to display waves.
  public static class Ball {
    public Vector2 pos;
    float nextY;
    float previousY;
    Ball(int i, float ballDiameter) {
      this.pos = new Vector2(i * ballDiameter, 0);
      this.nextY = this.previousY = 0;
    }
  }

  // mass per unit length
  private static final float MASS = 1;
  private static final float WAVE_SPEED = (float) Math.sqrt(1.0 /*TENSION*/ / MASS);
  //(WAVE_SPEED*delT/delX)*(WAVE_SPEED*delT/delX);
  private static final float ALPHA_SQUARE = 1;

  // Number of balls in the simulation
  private final int numBalls;
  // TODO: make this private
  // Diameter of each ball (in order to space them apart)
  public static final float BALL_DIAMETER = 1;
  // Array of balls to simulate wave on string
  // TODO: make this private
  public Ball balls[];
  //tension in the string
  private float tension = 8;
  // damping coefficient = b*delT/2
  private float beta = 0.05f;
  
  public WaveBox(ComponentType componentType, float x, float y, float rotation, int numBalls) {
    super(componentType, x, y, rotation);
    this.numBalls = numBalls;
    
    // balls on the string segment
    balls = new Ball[numBalls];
    for (int i = 0; i < numBalls; i++) {
      balls[i] = new Ball(i + 1, BALL_DIAMETER);
    }
  }

  public void singleStep(BoundaryType boundaryType) {
    switch (boundaryType) {
      case FixedEnd: 
        balls[numBalls - 1].pos.y = 0; break;
      case LooseEnd: 
        balls[numBalls - 1].pos.y = balls[numBalls - 2].pos.y; break;
      case NoEnd:    
        balls[numBalls - 1].pos.y = balls[numBalls - 2].previousY; break;
    }
    
    // Evolve according to 1D classical wave equation
    for(int i = 1; i < numBalls - 1; i++){
      balls[i].nextY = (1 / (1 + beta)) * (ALPHA_SQUARE * (balls[i+1].pos.y + balls[i-1].pos.y) + (beta - 1) * balls[i].previousY);
    }
    
    for(int i = 1; i < numBalls - 1; i++){
      balls[i].previousY = balls[i].pos.y;
      balls[i].pos.y = balls[i].nextY;
    }
    
    switch (boundaryType) {
      case FixedEnd:
        balls[numBalls - 1].previousY = 0;
        balls[numBalls - 1].pos.y = 0;
        break;
      case LooseEnd:
        balls[numBalls - 1].previousY = balls[numBalls - 1].pos.y;
        balls[numBalls - 1].pos.y = balls[numBalls - 2].pos.y;
        break;
      case NoEnd:
        balls[numBalls - 1].previousY = balls[numBalls - 1].pos.y;
        balls[numBalls - 1].pos.y = balls[numBalls - 1].pos.y;
        break;
    }
  }
  
  @Override
  public void reset() {
    super.reset();
    for (Ball b: balls) {
      b.pos.y = b.nextY = b.previousY = 0;
    }
  }

  public float getTension() {
    return tension;
  }

  public void setTension(float tension) {
    this.tension = tension;
  }

  public float getDamping() {
    return beta;
  }

  public void setDamping(float beta) {
    this.beta = beta;
  }
  
  @Override
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>(this, Parameter.Tension, 1, 10) {
      public Float getValue() { return getTension(); }
      public void setValue(Float value) { setTension(value); }
      public boolean isPossible() { return isActive(); }
    });

    configs.add(new AbstractModelConfig<Float>(this, Parameter.Damping, 0, 0.5f) {
      public Float getValue() { return getDamping(); }
      public void setValue(Float value) { setDamping(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
}
