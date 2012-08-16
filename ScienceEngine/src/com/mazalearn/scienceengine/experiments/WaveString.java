package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class WaveString extends Group implements Experiment {
  // Enum used for different Boundary Conditions on end of string
  public enum EndType { FixedEnd, LooseEnd, NoEnd };
  // Enum used for mode of wave generation
  public enum GenMode { Oscillate, Pulse, Manual};

  // Ball is a segment of the String used to display waves.
  static class Ball {
    Vector2 pos, displayPos;
    float nextY;
    float previousY;
    Ball(int i) {
      this.pos = new Vector2(i * BALL_DIAMETER, 0);
      this.nextY = this.previousY = 0;
      this.displayPos = new Vector2(this.pos.x + ORIGIN_X, ORIGIN_Y);
    }
  }
  private Actor startBall, endBall;
  
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
  private float amplitude = 3 * BALL_DIAMETER;
  // frequency of sinusoidal motion, units of cycles per frame
  private float frequency = 0.03f;
  //tension in the string
  private float tension = 10;
  // damping coefficient = b*delT/2
  private float beta = 0.05f;
  // Boundary condition of other end.
  private EndType endType = EndType.FixedEnd;
  // Generation mode
  private GenMode genMode = GenMode.Oscillate;
  // width of pulse
  private float pulseWidth = 10;

  private TextureRegion ballTexture;
  
  Ball balls[] = new Ball[NUM_BALLS];
  private Texture backgroundTexture;
  private int waveTime = 0;
  private boolean isPaused = false;
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
    startBall = new Image(ballTexture) {
      public boolean touchDown(float x, float y, int pointer) {
        return true;
      }
      public void touchDragged(float x, float y, int pointer) {
        balls[0].pos.y += y;
        WaveString.this.resume();
        return;
      }
    };
    endBall = new Image(ballTexture);
    startBall.x = balls[0].displayPos.x;
    endBall.x = balls[NUM_BALLS - 1].displayPos.x;
    addActor(startBall);
    addActor(endBall);
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
    balls[0].pos.y = (float) d;
    
    switch (this.endType) {
      case FixedEnd: 
        balls[NUM_BALLS - 1].pos.y = 0; break;
      case LooseEnd: 
        balls[NUM_BALLS - 1].pos.y = balls[NUM_BALLS - 2].pos.y; break;
      case NoEnd:    
        balls[NUM_BALLS - 1].pos.y = balls[NUM_BALLS - 2].previousY; break;
    }
    
    // Evolve according to 1D classical wave equation
    for(int i = 1; i < NUM_BALLS - 1; i++){
      balls[i].nextY = (1 / (1 + beta)) * (ALPHA_SQUARE * (balls[i+1].pos.y + balls[i-1].pos.y) + (beta - 1) * balls[i].previousY);
    }
    
    for(int i = 1; i < NUM_BALLS - 1; i++){
      balls[i].previousY = balls[i].pos.y;
      balls[i].pos.y = balls[i].nextY;
    }
    
    switch (this.endType) {
      case FixedEnd:
        balls[NUM_BALLS - 1].previousY = 0;
        balls[NUM_BALLS - 1].pos.y = 0;
        break;
      case LooseEnd:
        balls[NUM_BALLS - 1].previousY = balls[NUM_BALLS - 1].pos.y;
        balls[NUM_BALLS - 1].pos.y = balls[NUM_BALLS - 2].pos.y;
        break;
      case NoEnd:
        balls[NUM_BALLS - 1].previousY = balls[NUM_BALLS - 1].pos.y;
        balls[NUM_BALLS - 1].pos.y = balls[NUM_BALLS - 1].pos.y;
        break;
    }
  }
  
  private void simulateStep() {
    waveTime++;
    int frameCount = (int) (11 - tension);
    if (waveTime % frameCount == 0) {
      advance(balls[0].pos.y);
    }
    
    switch(genMode) {
      case Oscillate: balls[0].pos.y = (float) sinusoid(waveTime); break;
      case Pulse: balls[0].pos.y = (float) pulse(waveTime); break;
      case Manual: break;
    }
    
    float frameNumber = 1 + waveTime % frameCount;
    float ratio = frameNumber / frameCount;
    balls[0].displayPos.y = ORIGIN_Y + balls[0].pos.y;
    for (int i = 1; i < NUM_BALLS; i++) {
      Ball ball = balls[i];
      float y = (1 - ratio) * ball.previousY + ratio * ball.pos.y;
      //ball.pos.y = y;
      ball.displayPos.y = ORIGIN_Y + y;
    }
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

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw background
    batch.draw(backgroundTexture, this.x, this.y, this.width, this.height);
    // Advance n steps
    if (!isPaused ) {
      simulateStep();
    }
    startBall.y = balls[0].displayPos.y;
    endBall.y = balls[NUM_BALLS - 1].displayPos.y;
    // Draw the molecules
    for (Ball ball: balls) {
      batch.draw(ballTexture, this.x + ball.displayPos.x, this.y + ball.displayPos.y);
    }
    super.draw(batch, parentAlpha);
  }

  @Override
  public void reset() {
    waveTime = pulseStartTime = 0;
    for (Ball b: balls) {
      b.pos.y = b.nextY = b.previousY = 0;
      b.displayPos.y = ORIGIN_Y;
    }
  }

  @Override
  public void pause() {
    this.isPaused = true;
  }

  @Override
  public void resume() {
    this.isPaused = false;
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
      pulseStartTime = waveTime;
    }
  }

  @Override
  public boolean isPaused() {
    return isPaused;
  }
}
