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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Box2DActor - Box2D Actor
 * 
 * Map IBody to actor.
 * 
 */
public class Box2DActor extends Actor {
  protected static final int PIXELS_PER_M = 8;
  private ScienceBody body;
  private TextureRegion textureRegion;
  private float radius = 0;
  private float theta = 0;
  private Vector2 viewPos = new Vector2(), box2DPos = new Vector2();

  /**
   * Constructor.
   * 
   * @param body - Box2D body
   * @param textureRegion - texture to use to represent body in view
   */
  public Box2DActor(ScienceBody body, TextureRegion textureRegion) {
    super(body.getName());

    this.body = body;
    this.textureRegion = textureRegion;
    // Set the sprite width and height.
    this.width = textureRegion.getRegionWidth();
    this.height = textureRegion.getRegionHeight();
    this.originX = this.originY = 0;
  }
  
  /**
   * Set the origin to be used for rotation
   * @param originX
   * @param originY
   */
  public void setOrigin(float originX, float originY) {
    this.originX = originX;
    this.originY = originY;
    this.theta = (float) Math.atan2(originY, originX) * MathUtils.radiansToDegrees;
    this.radius = (float) Math.sqrt(originX * originX + originY * originY);
  }
  
  public ScienceBody getBody() {
    return body;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    getViewPositionFromBox2DPosition(viewPos);
    this.x = viewPos.x;
    this.y = viewPos.y;
    this.rotation = (body.getAngle() * MathUtils.radiansToDegrees) % 360;
    batch.draw(textureRegion, viewPos.x, viewPos.y, 0, 0, width, height, 1, 1, rotation);
  }

  @Override
  public Actor hit(float x, float y) {
    // x,y are in local coordinates
    return x > 0 && x < width && y > 0 && y < height ? this : null;
  }
  
  public TextureRegion getTextureRegion() {
    return textureRegion;
  }
  
  /**
   * @param viewPos (output) - position of view origin of body
   * position of body in scene2d view is output
   */
  public void getViewPositionFromBox2DPosition(Vector2 viewPos) {
    // screen position is at bottom left: (-originX, -originY) in local coords
    box2DPos.set(-originX, -originY).mul(1f / PIXELS_PER_M);
    viewPos.set(body.getWorldPoint(box2DPos));
    viewPos.mul(PIXELS_PER_M);
  }
  
  /**
   * Output box2d position of body if scene2d view position and rotation are as given
   * @param box2DPos (output)
   * @param viewPos - scene2d position of body
   * @param rotation - rotation of body
   */
  public void getBox2DPositionFromViewPosition(Vector2 box2DPos, Vector2 viewPos, float rotation) {
    box2DPos.x = (float) (viewPos.x + radius * MathUtils.cosDeg(rotation + theta));
    box2DPos.y = (float) (viewPos.y + radius * MathUtils.sinDeg(rotation + theta));
    box2DPos.mul(1f / PIXELS_PER_M);
  }
  
  public void setPositionFromViewCoords() {
    float angle = rotation * MathUtils.degreesToRadians;
    viewPos.set(x, y);
    getBox2DPositionFromViewPosition(box2DPos, viewPos, rotation);
    body.setPositionAndAngle(box2DPos, angle);
    body.setActive(visible);
    body.setInitial();
  }
}
