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
  private float radius;
  private float theta;
  private Vector2 newPos = new Vector2();

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
    this.theta = (float) Math.atan2(originY, originX) * MathUtils.radiansToDegrees;
    this.radius = (float) Math.sqrt(originX * originX + originY * originY);
  }
  
  public ScienceBody getBody() {
    return body;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    newPos.set(body.getPosition().x, body.getPosition().y);
    getViewPointFromWorld(newPos, body.getAngle());
    this.rotation = (body.getAngle() * MathUtils.radiansToDegrees) % 360;
    batch.draw(textureRegion, newPos.x, newPos.y, originX, originY, width, height, 1, 1, rotation);
  }

  @Override
  public Actor hit(float x, float y) {
    // x,y are in local coordinates
    return x > 0 && x < width && y > 0 && y < height ? this : null;
  }
  
  public TextureRegion getTextureRegion() {
    return textureRegion;
  }
  
  public Vector2 getViewPointFromWorld(Vector2 pos, float angle) {
    float radius = this.radius / PIXELS_PER_M;
    float theta = this.theta * MathUtils.degreesToRadians;
    pos.x = (float) (pos.x - radius * Math.cos(angle + theta));
    pos.y = (float) (pos.y - radius * Math.sin(angle + theta));
    pos.mul(PIXELS_PER_M);
    return pos;
  }
  
  public Vector2 getWorldPointFromView(Vector2 pos, float rotation) {
    pos.x = (float) (pos.x + radius * MathUtils.cos(rotation + theta));
    pos.y = (float) (pos.y + radius * MathUtils.sin(rotation + theta));
    pos.mul(1f / PIXELS_PER_M);
    return pos;
  }
  
  public void setPositionFromViewCoords() {
    float angle = rotation * MathUtils.degreesToRadians;
    newPos.set(x, y);
    body.setPositionAndAngle(getWorldPointFromView(newPos, rotation), angle);
    body.setActive(visible);
    body.setInitial();
  }
}
