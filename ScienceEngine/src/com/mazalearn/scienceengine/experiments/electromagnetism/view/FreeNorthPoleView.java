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
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FreeNorthPole.FieldSample;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FreeNorthPole;

public class FreeNorthPoleView extends Box2DActor {
  private final FreeNorthPole freeNorthPole;
  private Vector2 lastTouch = new Vector2();    // view coordinates
  private Vector3 currentTouch = new Vector3(); // view coordinates
  private TextureRegion arrow;
  private float radius;
    
  public FreeNorthPoleView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.radius = (float) Math.sqrt(width * width + height * height)/2;
    this.freeNorthPole = (FreeNorthPole) body;
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
    lastTouch.set(currentTouch.x, currentTouch.y);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    Color c = batch.getColor();
    for (FieldSample fieldSample: freeNorthPole.getFieldSamples()) {
      // Magnitude is scaled visually as color intensity
      batch.setColor(1, 1, 1, 0.25f + fieldSample.magnitude * 200);
      float rotation =  (fieldSample.angle * MathUtils.radiansToDegrees) % 360;
      batch.draw(arrow, fieldSample.x * PIXELS_PER_M, fieldSample.y * PIXELS_PER_M, 
          0, 0, width*0.2f, height*0.5f, 1, 1, rotation);
    }
    batch.setColor(c);
    super.draw(batch, parentAlpha);
  }
}