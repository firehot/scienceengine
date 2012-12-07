package com.mazalearn.scienceengine.domains.waves;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;

public class WaveModel extends AbstractScience2DModel {
  // Enum used for different Boundary Conditions on end of string
  public enum EndType { FixedEnd, LooseEnd, NoEnd };
  // Enum used for mode of wave generation
  public enum GenMode { Oscillate, Pulse, Manual};

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
  // Diameter of each ball (in order to space them apart)
  private final float BALL_DIAMETER = 1;
  // Array of balls to simulate wave on string
  public Ball balls[];
  // phase of sinusoidal motion, units of radians
  private float phi = 0;
  // Amplitude of sinusoidal motion
  private float amplitude;;
  // frequency of sinusoidal motion, units of cycles per frame
  private float frequency = 0.03f;
  //tension in the string
  private float tension = 8;
  // damping coefficient = b*delT/2
  private float beta = 0.05f;
  // Boundary iCondition of other end.
  private EndType endType = EndType.FixedEnd;
  // Generation mode
  private GenMode genMode = GenMode.Manual;
  // width of pulse
  private float pulseWidth = 10;

  // timeLimit of evolution of wave
  private int simulatedTime = 0;
  // timeLimit of evolution of a single pulse
  private int pulseStartTime = 0;
  
  public WaveModel(int numBalls) {
    this.numBalls = numBalls;
    
    amplitude = 3 * BALL_DIAMETER;   
    // balls on the string segment
    balls = new Ball[numBalls];
    for (int i = 0; i < numBalls; i++) {
      balls[i] = new Ball(i + 1, BALL_DIAMETER);
    }
  }

  private void singleStep(double d) {
    balls[0].pos.y = (float) d;
    
    switch (this.endType) {
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
    
    switch (this.endType) {
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
  public void singleStep() {
    simulatedTime++;
    int frameCount = (int) (11 - tension);
    if (simulatedTime % frameCount == 0) {
      singleStep(balls[0].pos.y);
    }
    
    switch(genMode) {
      case Oscillate: balls[0].pos.y = sinusoid(simulatedTime); break;
      case Pulse: balls[0].pos.y = pulse(simulatedTime); break;
      case Manual: break;
    }
  }

  private float sinusoid(int t) {
    return (float) (amplitude * Math.sin(2 * Math.PI * frequency * t + phi));
  }

  private float pulse(int waveTime) {
    int t = waveTime - pulseStartTime;
    float halfPulse = pulseWidth / 2;
    if (t < halfPulse) {
      return amplitude * t / halfPulse;
    } else if (t <= pulseWidth) {
      return amplitude * (2 - t / halfPulse);
    }
    return 0;
  }

  @Override
  public void reset() {
    super.reset();
    simulatedTime = pulseStartTime = 0;
    for (Ball b: balls) {
      b.pos.y = b.nextY = b.previousY = 0;
    }
  }

  public float getFrequency() {
    return frequency;
  }

  public void setFrequency(float frequency) {
    //ensures that sinusoid is continuous
    this.phi += 2 * Math.PI * simulatedTime * (this.frequency - frequency);
    this.frequency = frequency;
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

  public float getPulseWidth() {
    return pulseWidth;
  }

  public void setPulseWidth(float pulseWidth) {
    this.pulseWidth = pulseWidth;
  }

  public float getAmplitude() {
    return amplitude;
  }

  public void setAmplitude(float amplitude) {
    this.amplitude = amplitude;
  }

  public String getEndType() {
    return endType.name();
  }

  public void setEndType(String endType) {
    this.endType = EndType.valueOf(endType);
    reset();
  }

  public String getGenMode() {
    return genMode.name();
  }

  public void setGenMode(String genMode) {
    this.genMode = GenMode.valueOf(genMode);
    reset();
    if (this.genMode == GenMode.Pulse) {
      pulseStartTime = simulatedTime;
    }
  }

  @Override
  public void initializeConfigs(List<IModelConfig<?>> modelConfigs) {
    modelConfigs.add(new AbstractModelConfig<String>(null, Parameter.GenMode, GenMode.values()) {
     public String getValue() { return getGenMode(); }
     public void setValue(String value) { setGenMode(value); }
     public boolean isPossible() { return true; }
   });

   modelConfigs.add(new AbstractModelConfig<String>(null, Parameter.Boundary, EndType.values()) {
     public String getValue() { return getEndType(); }
     public void setValue(String value) { setEndType(value); }
     public boolean isPossible() { return true; }
   });

   modelConfigs.add(new AbstractModelConfig<Float>(null, Parameter.Frequency, 0, 1) {
      public Float getValue() { return getFrequency(); }
      public void setValue(Float value) { setFrequency(value); }
      public boolean isPossible() { return getGenMode() == "Oscillate";}
    });

    modelConfigs.add(new AbstractModelConfig<Float>(null, Parameter.Tension, 1, 10) {
      public Float getValue() { return getTension(); }
      public void setValue(Float value) { setTension(value); }
      public boolean isPossible() { return true; }
    });

    modelConfigs.add(new AbstractModelConfig<Float>(null, Parameter.PulseWidth, 5, 20) {
      public Float getValue() { return getPulseWidth(); }
      public void setValue(Float value) { setPulseWidth(value); }
      public boolean isPossible() { return getGenMode() == "Pulse";}
    });

    modelConfigs.add(new AbstractModelConfig<Float>(null, Parameter.Amplitude, 0, 100) {
      public Float getValue() { return getAmplitude(); }
      public void setValue(Float value) { setAmplitude(value); }
      public boolean isPossible() { return getGenMode() != "Manual"; }
    });

    modelConfigs.add(new AbstractModelConfig<Float>(null, Parameter.Damping, 0, 0.5f) {
      public Float getValue() { return getDamping(); }
      public void setValue(Float value) { setDamping(value); }
      public boolean isPossible() { return true; }
    });
  }

  @Override
  protected Science2DBody createScience2DBody(String componentTypeName,
      float x, float y, float rotation) {
    return null;
  }
  
  @Override
  public IComponentType componentNameToType(String componentTypeName) {
    return ComponentType.valueOf(componentTypeName);
  }
}
