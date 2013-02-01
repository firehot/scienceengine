package com.mazalearn.scienceengine.domains.waves.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class WaveMaker extends Science2DBody {
  // Enum used for mode of wave generation
  public enum GenMode { Oscillate, Pulse, Manual};
  // Generation mode
  private GenMode genMode = GenMode.Manual;
  // Amplitude of sinusoidal motion
  private float amplitude;;
  // frequency of sinusoidal motion, units of cycles per frame
  private float frequency = 0.03f;
  // width of pulse
  private float pulseWidth = 10;
  // timeLimit of evolution of a single pulse
  private long pulseStartTime = 0;
  // phase of sinusoidal motion, units of radians
  private float phi = 0;

  public WaveMaker(IComponentType componentType, float x, float y, float angle) {
    super(componentType, x, y, angle);
  }

  public String getGenMode() {
    return genMode.name();
  }

  public void setGenMode(String genMode) {
    this.genMode = GenMode.valueOf(genMode);
    reset();
    if (this.genMode == GenMode.Pulse) {
      pulseStartTime = ScienceEngine.getLogicalTime(); 
    }
  }
  
  @Override
  public void reset() {
    super.reset();
    pulseStartTime = 0;
  }

  @Override
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<String>(this, Parameter.GenMode, GenMode.values()) {
      public String getValue() { return getGenMode(); }
      public void setValue(String value) { setGenMode(value); }
      public boolean isPossible() { return isActive(); }
    });

    configs.add(new AbstractModelConfig<Float>(this, Parameter.Frequency, 0, 1) {
      public Float getValue() { return getFrequency(); }
      public void setValue(Float value) { setFrequency(value); }
      public boolean isPossible() { return isActive() && genMode == GenMode.Oscillate;}
    });

   configs.add(new AbstractModelConfig<Float>(this, Parameter.PulseWidth, 5, 20) {
     public Float getValue() { return getPulseWidth(); }
     public void setValue(Float value) { setPulseWidth(value); }
     public boolean isPossible() { return isActive() && genMode == GenMode.Pulse;}
   });

   configs.add(new AbstractModelConfig<Float>(this, Parameter.Amplitude, 0, 100) {
     public Float getValue() { return getAmplitude(); }
     public void setValue(Float value) { setAmplitude(value); }
     public boolean isPossible() { return isActive() && genMode != GenMode.Manual; }
   });

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

  public float getFrequency() {
    return frequency;
  }

  public void setFrequency(float frequency) {
    //ensures that sinusoid is continuous
    this.phi += 2 * Math.PI * ScienceEngine.getLogicalTime() * (this.frequency - frequency);
    this.frequency = frequency;
  }

  private float sinusoid(long t) {
    return (float) (amplitude * Math.sin(2 * Math.PI * frequency * t + phi));
  }

  private float pulse(long waveTime) {
    int t = (int) (waveTime - pulseStartTime);
    float halfPulse = pulseWidth / 2;
    if (t < halfPulse) {
      return amplitude * t / halfPulse;
    } else if (t <= pulseWidth) {
      return amplitude * (2 - t / halfPulse);
    }
    return 0;
  }

  public float getPos(long simulatedTime, float y) {
    switch(genMode) {
    case Oscillate: return sinusoid(simulatedTime);
    case Pulse: return pulse(simulatedTime);
    }
    return y;
  }

}
