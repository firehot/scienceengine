package com.mazalearn.scienceengine.box2d;

/*
 * -----------------------------------------------------------------------
 * Copyright 2012 - Sridhar Sundaram
 * -----------------------------------------------------------------------
 * 
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Box2DAction - Box2D Action
 * 
 * Map Box2D body to view coordinates.
 * 
 * Note: This action 'owns' body and will destroy it when finished. It normally
 * runs all the time against an active body.
 * 
 */
public class Box2DAction extends Action {
  private World world;
  private Body body;
  private Actor target;
  private float pixelsPerMetre;
  private boolean centred;

  /**
   * Constructor.
   * 
   * @param world - Box2D world
   * @param body - Box2D body
   * @param pixelsPerMetre - conversion ratio.
   * @param centred - true iff actor is to be centred in body.
   */
  public Box2DAction(World world, Body body, float pixelsPerMetre,
      boolean centred) {
    super();

    this.world = world;
    this.body = body;
    this.pixelsPerMetre = pixelsPerMetre;
    this.centred = centred;
  }

  /**
   * Action.
   * 
   */
  @Override
  public void act(float delta) {
    if (!isDone()) {
      // Centre body.
      Vector2 pos = body.getPosition();

      if (centred) { // Adjust the actor to centre
        target.x = (pos.x * pixelsPerMetre) - target.width / 2;
        target.y = (pos.y * pixelsPerMetre) - target.height / 2;
      } else {
        target.x = (pos.x * pixelsPerMetre);
        target.y = (pos.y * pixelsPerMetre);
      }

      float angleDeg = body.getAngle() * MathUtils.radiansToDegrees;

      target.rotation = angleDeg;
    } else {
      Gdx.app.log("Box2DAction", "!!!Executing finished action, " + hashCode());
    }
  }

  @Override
  public boolean isDone() {
    // TODO
    return false;
  }

  @Override
  public void setTarget(Actor actor) {
    this.target = actor;
  }

  @Override
  public Actor getTarget() {
    return target;
  }

  /**
   * Handle action finish.
   * 
   * This will be called when the associated Actor is being culled from view.
   * Note that we remove the associated body from the Box2D world at the same
   * time.
   */
  @Override
  public void finish() {
    super.finish();

    if (world != null) {
      world.destroyBody(body);
    }
  }

  @Override
  public Action copy() {
    return null;
  }

}
