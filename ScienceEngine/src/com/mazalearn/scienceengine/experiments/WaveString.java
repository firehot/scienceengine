package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class WaveString extends Actor implements Experiment {
  // Enum used for different Boundary Conditions on end of string
  public enum EndType { FixedEnd, LooseEnd, NoEnd };
  // Enum used for mode of wave generation
  public enum GenMode { Oscillate, Pulse};

  // Ball is a segment of the String used to display waves.
  static class Ball {
    float x;
    float y;
    float nextY;
    float previousY;
    Ball(float x) {
      this.x = x;
      this.y = this.nextY = this.previousY = 0;
    }
  }
  
  private static final float ORIGIN_Y = 80f;
  private static final float ORIGIN_X = 1f;
  private static final int BALL_DIAMETER = 8;

  // mass per unit length
  private static final float MASS = 1;
  private static int NUM_BALLS = 40;
  private static final float WAVE_SPEED = (float) Math.sqrt(1.0 /*TENSION*/ / MASS);
  //(WAVE_SPEED*delT/delX)*(WAVE_SPEED*delT/delX);
  private static final float ALPHA_SQUARE = 1;

  // phase of sinusoidal motion, units of radians
  private float phi = 0;
  // Amplitude of sinusoidal motion
  private float amplitude = 3;
  // frequency of sinusoidal motion, units of cycles per frame
  private float frequency = 0.03f;
  //tension in the string
  private float tension = 10;
  // damping coefficient = b*delT/2
  private float beta = 0.01f;
  // Boundary condition of other end.
  private EndType endType = EndType.LooseEnd;
  // Generation mode
  private GenMode genMode = GenMode.Oscillate;
  // width of pulse
  private float pulseWidth = 10;

  private TextureRegion ballTexture;
  
  Ball balls[] = new Ball[NUM_BALLS];
  private Texture backgroundTexture;
  private int waveTime = 0;
  private Ball endBall;
  private boolean paused = false;
  private int pulseStartTime = 0;
  
  public WaveString(float width, float height) {
    this.width = width;
    this.height = height;
    
    ballTexture = createBallTexture();
    // Use light-gray background color
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0, 0, 1, 1);
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    
    // balls on the string segment
    for (int i = 0; i < NUM_BALLS; i++) {
      balls[i] = new Ball(i + 1);
    }
    endBall = balls[NUM_BALLS - 1];
  }

  private TextureRegion createBallTexture() {
    // Create texture region for ball
    Pixmap pixmap = new Pixmap(BALL_DIAMETER, BALL_DIAMETER, Format.RGBA8888);
    pixmap.setColor(Color.RED);
    pixmap.fillCircle(BALL_DIAMETER/2, BALL_DIAMETER/2, BALL_DIAMETER/2);
    TextureRegion ballTexture = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return ballTexture;
  }
  
  public void advance(double d) {
    balls[0].y = (float) d;
    
    switch (this.endType) {
      case FixedEnd:
        endBall.y = 0; break;
      case LooseEnd:
        endBall.y = balls[NUM_BALLS - 2].y; break;
      case NoEnd:
        endBall.y = balls[NUM_BALLS - 2].previousY; break;
    }
    
    // Evolve according to 1D classical wave equation
    for(int i = 1; i < NUM_BALLS - 1; i++){
      balls[i].nextY = (1 / (1 + beta)) * (ALPHA_SQUARE * (balls[i+1].y + balls[i-1].y) + (beta - 1) * balls[i].previousY);
    }
    
    for(int i = 1; i < NUM_BALLS - 1; i++){
      balls[i].previousY = balls[i].y;
      balls[i].y = balls[i].nextY;
    }
    
    switch (this.endType) {
      case FixedEnd:
        endBall.previousY = 0;
        endBall.y = 0;
        break;
      case LooseEnd:
        endBall.previousY = endBall.y;
        endBall.y = balls[NUM_BALLS - 2].y;
        break;
      case NoEnd:
        endBall.previousY = endBall.y;
        endBall.y = endBall.y;
        break;
    }
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw background
    batch.draw(backgroundTexture, this.x, this.y, this.width, this.height);
    // Advance n steps
    if (!paused ) {
      waveTime++;
      int count = (int) (11 - tension);
      if (waveTime % count != 0) return;
      switch(genMode) {
        case Oscillate: advance(sinusoid(waveTime)); break;
        case Pulse: advance(pulse(waveTime)); break;
      }
    }
    // Draw the molecules
    for (Ball ball: balls) {
      batch.draw(ballTexture, 
          this.x + ORIGIN_X + (float) (ball.x * BALL_DIAMETER), 
          this.y + ORIGIN_Y + (float) (ball.y * BALL_DIAMETER));
    }
  }

  @Override
  public Actor hit(float x, float y) {
    return null;
  }

  private double sinusoid(int t) {
    return amplitude * Math.sin(2 * Math.PI * frequency * t + phi);
  }

  private double pulse(int waveTime) {
    int t = waveTime - pulseStartTime;
    double halfPulse = pulseWidth / 2;
    if (t < halfPulse) {
      return amplitude * t / halfPulse;
    } else if (t <= pulseWidth) {
      return amplitude * (2 - t / halfPulse);
    }
    return 0;
  }

  public float getFrequency() {
    return frequency;
  }

  public void setFrequency(float frequency) {
    //ensures that sinusoid is continuous
    this.phi += 2 * Math.PI * waveTime * (this.frequency - frequency);
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

  public EndType getBoundaryCondition() {
    return endType;
  }

  public void setBoundaryCondition(EndType endBC) {
    this.endType = endBC;
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

  @Override
  public void reset() {
    waveTime = pulseStartTime = 0;
    for (Ball b: balls) {
      b.y = b.nextY = b.previousY = 0;

    }
  }

  @Override
  public void pause() {
    this.paused = true;
  }

  @Override
  public void resume() {
    this.paused = false;
  }

  public String getEndType() {
    return endType.name();
  }

  public void setEndType(String endType) {
    this.endType = EndType.valueOf(endType);
  }

  public String getGenMode() {
    return genMode.name();
  }

  public void setGenMode(String genMode) {
    this.genMode = GenMode.valueOf(genMode);
    if (this.genMode == GenMode.Pulse) {
      pulseStartTime = waveTime;
    }
  }
}
