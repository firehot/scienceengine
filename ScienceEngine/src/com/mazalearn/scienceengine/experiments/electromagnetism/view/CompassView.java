package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;

public class CompassView extends Box2DActor {
  private final Compass compass;
  private Vector2 lastTouch = new Vector2();
  private Vector3 vector3 = new Vector3();
  private static final int NUM_POINTS = 100;
  private float[][] fieldSamples = new float[NUM_POINTS][4];
  private int count = 0;
  private TextureRegion arrow;
  
  public CompassView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.width /= 2;
    this.height /= 2;
    this.compass = (Compass) body;
    this.originX = width/2;
    this.originY = height/2;
    arrow = new TextureRegion(new Texture("images/arrow.png"));
  }

  @Override
  public boolean touchDown(float localX, float localY, int pointer) {
    vector3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    getStage().getCamera().unproject(vector3);
    lastTouch.set(vector3.x, vector3.y);
    return true;
  }

  @Override
  public void touchDragged(float localX, float localY, int pointer) {
    vector3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    getStage().getCamera().unproject(vector3);
    lastTouch.sub(vector3.x, vector3.y);
    this.x -= lastTouch.x;
    this.y -= lastTouch.y;
    setPositionFromScreen();
    compass.singleStep(0);
    lastTouch.set(vector3.x, vector3.y);
  }
  
  @Override
  public void touchUp(float localX, float localY, int pointer) {
    // Record field at this position
    fieldSamples[count][0] = lastTouch.x;
    fieldSamples[count][1] = lastTouch.y;
    fieldSamples[count][2] = (compass.getAngle() * MathUtils.radiansToDegrees) % 360;
    fieldSamples[count][3] = compass.getBField();
    
    count = (count + 1) % NUM_POINTS;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    for (int i = 0; i < fieldSamples.length; i++) {
      if (fieldSamples[i][0] == 0) break;
      batch.draw(arrow, fieldSamples[i][0], fieldSamples[i][1], 0, 0, 30, 30, 1, 1, fieldSamples[i][2]);
    }
  }
}