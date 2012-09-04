package com.mazalearn.scienceengine.box2d;

/*
 * -----------------------------------------------------------------------
 * Copyright 2012 - Sridhar Sundaram
 * -----------------------------------------------------------------------
 * 
 */

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Box2DActor - Box2D Actor
 * 
 * Map IBody to actor.
 * 
 */
public class Box2DActor extends Actor {
  private static final int PIXELS_PER_M = 8;
  private ScienceBody body;
  private TextureRegion textureRegion;

  /**
   * Constructor.
   * 
   * @param body - Box2D body
   * @param textureRegion - texture to use to represent body in view
   */
  public Box2DActor(ScienceBody body, TextureRegion textureRegion) {
    super();

    this.body = body;
    this.textureRegion = textureRegion;
    // Set the sprite width and height.
    this.width = textureRegion.getRegionWidth();
    this.height = textureRegion.getRegionHeight();
  }
  
  public ScienceBody getBody() {
    return body;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.x = body.getPosition().x * PIXELS_PER_M;
    this.y = body.getPosition().y * PIXELS_PER_M;
    this.rotation = (body.getAngle() * MathUtils.radiansToDegrees) % 360;
    batch.draw(textureRegion, x, y, 0, 0, width, height, 1, 1, rotation);
  }

  @Override
  public Actor hit(float x, float y) {
    // x,y are in local coordinates
    return x > 0 && x < width && y > 0 && y < height ? this : null;
  }
  
  @Override
  public boolean touchDown(float x, float y, int pointer) {
    return false;    
  }
  
  public TextureRegion getTextureRegion() {
    return textureRegion;
  }
  
  public void setPositionFromScreen() {
    body.setPositionAndAngle(x / PIXELS_PER_M, y / PIXELS_PER_M, body.getAngle());
  }
}
