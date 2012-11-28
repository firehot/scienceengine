package com.mazalearn.scienceengine.core.view;

/*
 * -----------------------------------------------------------------------
 * Copyright 2012 - Sridhar Sundaram
 * -----------------------------------------------------------------------
 * 
 */

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Science2DActor - Takes as model a Science2DBody which is a Box2D body
 *                and creates an actor view for it.
 * 
 * 
 */
public class Science2DActor extends Actor {
  private Science2DBody body;
  private TextureRegion textureRegion;
  private Vector2 viewPos = new Vector2(), box2DPos = new Vector2();
  protected Vector2 lastTouch = new Vector2();    // view coordinates
  private boolean allowMove = false;

  /**
   * Constructor.
   * 
   * @param body - Box2D body
   * @param textureRegion - texture to use to represent body in view
   */
  public Science2DActor(final Science2DBody body, TextureRegion textureRegion) {
    super();
    this.setName(body.name());
    this.body = body;
    this.textureRegion = textureRegion;
    // Set the sprite width and height.
    this.setSize(textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
    this.setOrigin(getWidth() / 2, getHeight() / 2);
    ClickListener touchLlistener = new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        if (!allowMove) return false;
        lastTouch.set(event.getStageX(), event.getStageY());
        return true;
      }

      @Override
      public void touchDragged(InputEvent event, float localX, float localY, int pointer) {
        // Get negative of movement vector
        lastTouch.sub(event.getStageX(), event.getStageY());
        Science2DActor.this.setPosition(getX() - lastTouch.x, getY() - lastTouch.y);
        setPositionFromViewCoords(true);
        // Recalibrate lastTouch to new coordinates
        lastTouch.set(event.getStageX(), event.getStageY());
      }

    };
    this.addListener(touchLlistener);
    ClickListener helpListener = new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        super.touchDown(event, localX, localY, pointer, button);
        ScienceEngine.setSelectedBody(body);
        IScience2DStage stage = (IScience2DStage) getStage();
        Label status = (Label) stage.findActor(StageComponent.Status.name());
        IComponentType componentType = body.getComponentType();
        status.setText(
            body.toString() + "  -  " +
            ScienceEngine.getMsg().getString("Help." + componentType.name()));
        return false;
      }
    };
    this.addListener(helpListener);     
  }
  
  public Science2DBody getBody() {
    return body;
  }
  
  @Override
  public void act(float delta) {
    getViewPositionFromBox2DPosition(viewPos);
    this.setX(viewPos.x);
    this.setY(viewPos.y);
    this.setRotation((body.getAngle() * MathUtils.radiansToDegrees) % 360);
    super.act(delta);
  }

  /**
   * return name of the body associated with the actor
   */
  @Override
  public String getName() {
    return body.name();
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    batch.draw(textureRegion, getX(), getY(), this.getOriginX(), 
        this.getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
    // debugDraw(batch);
  }

  float px, py, pbx, pby, pa;
  public void debugDraw(SpriteBatch batch) {
    Color c = batch.getColor();
    batch.setColor(Color.RED);
    batch.draw(textureRegion, getX(), getY(), 0, 0, getWidth(), getHeight(), 1, 1, getRotation());
    batch.setColor(c);
    if (getX() != px || getY() != py || body.getPosition().x != pbx || body.getPosition().y != pby || body.getAngle() != pa) {
      System.out.println(getName() + " x = " + getX() + " y = " + getY() + 
          " origin x = " + this.getOriginX() + " origin y = " + this.getOriginY() + 
          " body = " + body.getPosition() + " angle = " + body.getAngle());
      px = getX(); py = getY(); pbx = body.getPosition().x; pby = body.getPosition().y; pa = body.getAngle();
    }
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
    viewPos.sub(getOriginX(), getOriginY());
  }
  
  /**
   * Output box2d position of body if scene2d view position and rotation are as given
   * @param box2DPos (output)
   * @param viewPos - scene2d position of body
   * @param rotation - rotation of body
   */
  public void getBox2DPositionFromViewPosition(Vector2 box2DPos, Vector2 viewPos, float rotation) {
    box2DPos.set(viewPos.x, viewPos.y);
    box2DPos.add(getOriginX(), getOriginY());
    box2DPos.mul(1f / ScienceEngine.PIXELS_PER_M);
  }
  
  public void setPositionFromViewCoords(boolean isUserChange) {
    viewPos.set(getX(), getY());
    getBox2DPositionFromViewPosition(box2DPos, viewPos, getRotation());
    float angle = getRotation() * MathUtils.degreesToRadians;
    if (isUserChange) { // Change initiated by user, hence propagate
      ((AbstractScience2DStage) getStage()).notifyLocationChangedByUser(this, box2DPos, angle);
    }
    body.setPositionAndAngle(box2DPos, angle);
    body.setActive(isVisible());
    if (!isUserChange) {
      body.setInitial();
    }
  }
  
  public boolean isAllowMove() {
    return allowMove;
  }

  public void setAllowMove(boolean allowMove) {
    this.allowMove = allowMove;
  }
}
