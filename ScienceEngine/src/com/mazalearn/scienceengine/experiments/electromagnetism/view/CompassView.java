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
  private Vector2 lastTouch = new Vector2();    // view coordinates
  private Vector3 currentTouch = new Vector3(); // view coordinates
  private TextureRegion arrow;
  private float radius;
    
  public CompassView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.width /= 2;
    this.height /= 2;
    this.radius = (float) Math.sqrt(width * width + height * height)/2;
    this.compass = (Compass) body;
    this.originX = width/2;
    this.originY = height/2;
    arrow = new TextureRegion(new Texture("images/arrow.png"));
  }

  @Override
  public boolean touchDown(float localX, float localY, int pointer) {
    currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    getStage().getCamera().unproject(currentTouch);
    lastTouch.set(currentTouch.x, currentTouch.y);
    return true;
  }

  @Override
  public void touchDragged(float localX, float localY, int pointer) {
    currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    getStage().getCamera().unproject(currentTouch);
    lastTouch.sub(currentTouch.x, currentTouch.y);
    this.x -= lastTouch.x;
    this.y -= lastTouch.y;
    setPositionFromViewCoords();
    compass.singleStep(0);
    lastTouch.set(currentTouch.x, currentTouch.y);
  }
  
  @Override
  public void touchUp(float localX, float localY, int pointer) {
    // Record field at this position as a world point
    Vector2 pos = new Vector2(), pos2;
    pos.set(-width/2, -height/2);
    pos.mul(1f/PIXELS_PER_M);
    pos2 = compass.getWorldPoint(pos);
    compass.addFieldSample(pos2.x * PIXELS_PER_M,  pos2.y * PIXELS_PER_M);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    Color c = batch.getColor();
    batch.setColor(1, 1, 1, 0.75f * parentAlpha);
    for (FieldSample fieldSample: compass.getFieldSamples()) {
      // Magnitude is scaled visually as thickness of arrow to show field strength.
      float scale = fieldSample.magnitude * 10; // fieldSample.magnitude > 0.02f ? 1 : fieldSample.magnitude * 50;
      float rotation =  (fieldSample.angle * MathUtils.radiansToDegrees) % 360;
      batch.draw(arrow, fieldSample.x, fieldSample.y, 
          0, 0, width, height, 1, scale, rotation);
    }
    batch.setColor(c);
    super.draw(batch, parentAlpha);
  }
}