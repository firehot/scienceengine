package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.view.ScienceActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.FieldMeterActor;

public abstract class AbstractProber extends Group {
  static final float TOLERANCE = 0.3f;
  static final float ZERO_TOLERANCE = 1e-4f;
  private final Vector2 modelPos = new Vector2();
  private final IExperimentModel model;
  private final List<Actor> excludedActors;
  protected FieldMeter fieldMeter;
  protected ScienceActor fieldMeterActor;
 
  protected AbstractProber(IExperimentModel model, List<Actor> actors, Actor dashboard) {
    this.model = model;
    this.excludedActors = new ArrayList<Actor>();
    excludedActors.add(dashboard);
    for (Actor actor: actors) {
      if (actor.name == "FieldMeter") {
        this.fieldMeterActor = (ScienceActor) actor;
        this.fieldMeter = (FieldMeter) fieldMeterActor.getBody();
      } else if (actor.visible) {
        excludedActors.add(actor);
      }
    }
  }
  public abstract void activate(boolean activate);
  
  public abstract String getTitle();
  
  private boolean areTooClose(Vector2[] points) {
    if (points.length < 2) return false;
    return approxEquals(points[0].len(), points[1].len()) && 
        (approxEquals(points[0].x, points[1].x) || approxEquals(points[0].y, points[1].y));
  }
  
  protected boolean haveSimilarMagnitudes(float v1, float v2) {
    if (Math.abs(v1 - v2) < ZERO_TOLERANCE) return true;
    if (Math.abs(v1 - v2) / Math.min(v1, v2) < TOLERANCE) return true;
    return false;
  }
  
  private boolean isInsideExcludedActor(Vector2 point) {
    for (Actor actor: excludedActors) {
      float actorWidth = actor.width;
      float actorHeight = actor.height;
      if (actor instanceof Table) {
        actorWidth = ((Table) actor).getPrefWidth();
        actorHeight = ((Table) actor).getPrefHeight();
      } 
      if (point.x >= actor.x && point.x <= actor.x + actorWidth &&
          point.y >= actor.y && point.y <= actor.y + actorHeight) {
        return true;
      }
    }
    return false;
  }
  
  protected void generateProbePoints(Vector2... points) {
    do {
      for (Vector2 point: points) {
        do {
          point.set(MathUtils.random(), MathUtils.random());
          point.x *= width;
          point.y *= height;
          point.add(x, y);
        } while (isInsideExcludedActor(point));
      }
    } while (areTooClose(points) || !arePointsAcceptable(points));
  }
  
  protected void getBField(Vector2 viewPos, Vector2 bField) {
    modelPos.set(viewPos).mul(1f / ScienceEngine.PIXELS_PER_M);
    model.getBField(modelPos, bField /* output */);
  }
  
  protected abstract boolean arePointsAcceptable(Vector2[] points);

  private boolean approxEquals(float len1, float len2) {
    return Math.abs(len1 - len2) < TOLERANCE;
  }
}
