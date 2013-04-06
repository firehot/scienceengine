package com.mazalearn.scienceengine.tutor;


import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public abstract class AbstractScience2DProber extends AbstractTutor {

  protected static final float TOLERANCE = 0.3f;
  protected static final float ZERO_TOLERANCE = 1e-4f;
  private Vector2 localPoint = new Vector2();

  public AbstractScience2DProber(IScience2DController science2DController,
      ITutorType tutorType, ITutor parent, String goal, String id, Array<?> components, Array<?> configs,
      int successPoints, int failurePoints, String[] hints, String explanation) {
    super(science2DController, tutorType, parent, goal, id, components, configs, 
        successPoints, failurePoints, hints, explanation);
    // A prober covers the entire screen
    this.setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
  }
  
  private boolean areTooClose(Vector2[] points) {
    if (points.length < 2) return false;
    return approxEquals(points[0].len(), points[1].len()) && 
        (approxEquals(points[0].x, points[1].x) || approxEquals(points[0].y, points[1].y));
  }

  // protected for testing
  protected boolean isInsideExcludedActor(Vector2 stagePoint, List<Actor> excludedActors) {
    for (Actor actor: excludedActors) {
      // Translate to local coordinates of actor
      actor.stageToLocalCoordinates(localPoint.set(stagePoint));
      Actor hitActor = actor.hit(localPoint.x, localPoint.y, true);
      if (hitActor != null) {
        // If this actor spans entire screen, ignore it
        hitActor.stageToLocalCoordinates(localPoint.set(0, 0));
        boolean bottomLeftHit = hitActor.hit(localPoint.x, localPoint.y, true) == hitActor;
        actor.stageToLocalCoordinates(localPoint.set(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT));
        boolean topRightHit = hitActor.hit(localPoint.x, localPoint.y, true) == hitActor; 
        if (!bottomLeftHit || !topRightHit) {
          return true;
        }
      }
      // Since space in side table will not get hit, we check explicitly
      // For a table, x and y are at center of table - not at bottom left
      if (actor instanceof Table) {
        float actorWidth = ((Table) actor).getPrefWidth();
        float actorHeight = ((Table) actor).getPrefHeight();
        if (stagePoint.x >= actor.getX() - actorWidth/ 2 && stagePoint.x <= actor.getX() + actorWidth/2 &&
            stagePoint.y >= actor.getY() - actorHeight/2 && stagePoint.y <= actor.getY() + actorHeight/2) {
          return true;
        }
      }
      if (ScienceEngine.DEV_MODE == DevMode.DEBUG)
        System.out.println(actor.getClass().getName() + " " + actor.getName() + 
            " Stagepoint: " + stagePoint + " localpoint: " + localPoint);
    }
    return false;
  }

  // Probe points are generated in stage coordinates not too close to periphery
  // Then checked for being outside of excluded actors
  // Then checked for being too close to each other
  protected void generateProbePoints(Vector2[] points) {
    List<Actor> excludedActors = science2DController.getGuru().getExcludedActors();
    do {
      for (int i = 0; i < points.length; i++) {
        Vector2 point = points[i];
        do {
          // random point in ([0,1],[0,1])
          point.set(MathUtils.random(), MathUtils.random());
          // Transform point to [ 0.8 x width, 0.8 x height]
          point.x *= getWidth() * 0.8f;
          point.y *= getHeight() * 0.8f;
          // Move point to [0.1 x width, 0.9 x width],[0.1 x height,0.9 x height]
          point.add(getX() + 0.1f * getWidth(), getY() + 0.1f * getHeight());
        } while (isInsideExcludedActor(point, excludedActors));
      }
    } while (areTooClose(points));    
  }

  private boolean approxEquals(float len1, float len2) {
    return Math.abs(len1 - len2) < TOLERANCE;
  }
}