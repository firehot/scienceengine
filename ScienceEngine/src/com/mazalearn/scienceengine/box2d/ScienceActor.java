package com.mazalearn.scienceengine.box2d;

/*
 * -----------------------------------------------------------------------
 * Copyright 2012 - Sridhar Sundaram
 * -----------------------------------------------------------------------
 * 
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

/**
 * ScienceActor - Takes as model a ScienceBody which is a Box2D body
 *                and creates an actor view for it.
 * 
 * 
 */
public class ScienceActor extends Actor {
  private ScienceBody body;
  private TextureRegion textureRegion;
  private Vector2 viewPos = new Vector2(), box2DPos = new Vector2();
  protected Vector2 lastTouch = new Vector2();    // view coordinates
  protected Vector3 currentTouch = new Vector3(); // view coordinates
  private boolean allowDrag = false;

  /**
   * Constructor.
   * 
   * @param body - Box2D body
   * @param textureRegion - texture to use to represent body in view
   */
  public ScienceActor(ScienceBody body, TextureRegion textureRegion) {
    super(body.getName());

    this.body = body;
    this.textureRegion = textureRegion;
    // Set the sprite width and height.
    this.width = textureRegion.getRegionWidth();
    this.height = textureRegion.getRegionHeight();
    this.originX = width / 2;
    this.originY = height / 2;
  }
  
  public ScienceBody getBody() {
    return body;
  }
  
  @Override
  public void act(float delta) {
    getViewPositionFromBox2DPosition(viewPos);
    this.x = viewPos.x;
    this.y = viewPos.y;
    this.rotation = (body.getAngle() * MathUtils.radiansToDegrees) % 360;
    super.act(delta);
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    batch.draw(textureRegion, x, y, this.originX, this.originY, width, height, 1, 1, rotation);
    // debugDraw(batch);
  }

  float px, py, pbx, pby, pa;
  public void debugDraw(SpriteBatch batch) {
    Color c = batch.getColor();
    batch.setColor(Color.RED);
    batch.draw(textureRegion, x, y, 0, 0, width, height, 1, 1, rotation);
    batch.setColor(c);
    if (x != px || y != py || body.getPosition().x != pbx || body.getPosition().y != pby || body.getAngle() != pa) {
      System.out.println(body.getName() + " x = " + x + " y = " + y + 
          " origin x = " + this.originX + " origin y = " + this.originY + 
          " body = " + body.getPosition() + " angle = " + body.getAngle());
      px = x; py = y; pbx = body.getPosition().x; pby = body.getPosition().y; pa = body.getAngle();
    }
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
   * ASSUMPTION: ORIGIN is BOX2D position for rotating body and does not 
   * change under rotation.
   */
  public void getViewPositionFromBox2DPosition(Vector2 viewPos) {
    viewPos.set(body.getWorldCenter());
    viewPos.mul(ScienceEngine.PIXELS_PER_M);
    viewPos.sub(originX, originY);
  }
  
  /**
   * Output box2d position of body if scene2d view position and rotation are as given
   * @param box2DPos (output)
   * @param viewPos - scene2d position of body
   * @param rotation - rotation of body
   */
  public void getBox2DPositionFromViewPosition(Vector2 box2DPos, Vector2 viewPos, float rotation) {
    box2DPos.set(viewPos.x, viewPos.y);
    box2DPos.add(originX, originY);
    box2DPos.mul(1f / ScienceEngine.PIXELS_PER_M);
  }
  
  public void setPositionFromViewCoords(boolean isUserChange) {
    viewPos.set(x, y);
    getBox2DPositionFromViewPosition(box2DPos, viewPos, rotation);
    if (isUserChange) { // Change initiated by user, hence propagate
      ((AbstractExperimentView) getStage()).notifyLocationChangedByUser(this, box2DPos);
    }
    float angle = rotation * MathUtils.degreesToRadians;
    body.setPositionAndAngle(box2DPos, angle);
    body.setActive(visible);
    body.setInitial();
  }
  
  @Override
  public boolean touchDown(float localX, float localY, int pointer) {
    if (!allowDrag) return false;
    currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    getStage().getCamera().unproject(currentTouch);
    lastTouch.set(currentTouch.x, currentTouch.y);
    return true;
  }

  @Override
  public void touchDragged(float localX, float localY, int pointer) {
    // Screen coords of current touch
    currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    // Screen coords of current touch
    getStage().getCamera().unproject(currentTouch);
    // Get negative of movement vector
   lastTouch.sub(currentTouch.x, currentTouch.y);
    this.x -= lastTouch.x;
    this.y -= lastTouch.y;
    setPositionFromViewCoords(true);
    // Recalibrate lastTouch to new coordinates
    lastTouch.set(currentTouch.x, currentTouch.y);
  }

  public boolean isAllowDrag() {
    return allowDrag;
  }

  public void setAllowDrag(boolean allowDrag) {
    this.allowDrag = allowDrag;
  }
}
