package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass.FieldSample;

public class CompassView extends Box2DActor {
  private final Compass compass;
  private Vector2 lastTouch = new Vector2();
  private Vector3 vector3 = new Vector3();
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
    compass.addFieldSample(lastTouch);     
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    Color c = batch.getColor();
    for (FieldSample fieldSample: compass.getFieldSamples()) {
      float intensity = 0.25f + fieldSample.magnitude * 200;
      float rotation =  (fieldSample.angle * MathUtils.radiansToDegrees) % 360;
      batch.setColor(1, 1, 1, intensity);
      batch.draw(arrow, fieldSample.x, fieldSample.y, 
          0, 0, 30, 30, 1, 1, rotation);
    }
    batch.setColor(c);
  }
}