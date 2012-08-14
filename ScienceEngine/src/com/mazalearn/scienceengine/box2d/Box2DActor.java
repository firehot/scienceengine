package com.mazalearn.scienceengine.box2d;

/*
 * -----------------------------------------------------------------------
 * Copyright 2012 - Sridhar Sundaram
 * -----------------------------------------------------------------------
 * 
 */

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Box2DActor - Box2D Actor
 * 
 * Map Box2D body to actor.
 * 
 * ??????
 * Note: This actor 'owns' body and will destroy it when finished. It normally
 * runs all the time against an active body.
 * 
 */
public class Box2DActor extends Actor {
  private Body body;
  private TextureRegion textureRegion;

  /**
   * Constructor.
   * 
   * @param body - Box2D body
   * @param textureRegion - texture to use to represent body in view
   */
  public Box2DActor(Body body, TextureRegion textureRegion) {
    super();

    this.body = body;
    this.textureRegion = textureRegion;
    // Set the sprite width and height.
    this.width = textureRegion.getRegionWidth();
    this.height = textureRegion.getRegionHeight();
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    x = body.getPosition().x * 8;
    y = body.getPosition().y * 8;
    batch.draw(textureRegion, x, y);
  }

  @Override
  public Actor hit(float x, float y) {
    return x > 0 && x < width && y > 0 && y < height ? this : null;
  }
  
  @Override
  public boolean touchDown(float x, float y, int pointer) {
    return false;    
  }
}
