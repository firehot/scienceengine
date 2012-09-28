package com.mazalearn.scienceengine.core.probe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

public abstract class AbstractProber extends Group {

  protected static final float TOLERANCE = 0.3f;
  protected static final float ZERO_TOLERANCE = 1e-4f;
  protected final ProbeManager probeManager;

  public AbstractProber(ProbeManager probeManager) {
    this.probeManager = probeManager;
    probeManager.registerProber(this);
  }
  
  public void reinitialize(float x, float y, float width, float height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height; 
  }

  public abstract void activate(boolean activate);

  public abstract String getTitle();

  private boolean areTooClose(Vector2[] points) {
    if (points.length < 2) return false;
    return approxEquals(points[0].len(), points[1].len()) && 
        (approxEquals(points[0].x, points[1].x) || approxEquals(points[0].y, points[1].y));
  }

  private boolean isInsideExcludedActor(Vector2 point) {
    for (Actor actor: probeManager.getExcludedActors()) {
      // For a table, x and y are at center, top of table - not at bottom left
      if (actor instanceof Table) {
        float actorWidth = ((Table) actor).getPrefWidth();
        float actorHeight = ((Table) actor).getPrefHeight();
        if (point.x >= actor.x - actorWidth/ 2 && point.x <= actor.x + actorWidth/2 &&
            point.y <= actor.y && point.y >= actor.y - actorHeight) {
          return true;
        }
      } else {
        if (point.x >= actor.x && point.x <= actor.x + actor.width &&
            point.y >= actor.y && point.y <= actor.y + actor.height) {
          return true;
        }
        
      }
    }
    return false;
  }

  protected void generateProbePoints(Vector2[] points) {
    do {
      for (int i = 0; i < points.length; i++) {
        Vector2 point = points[i];
        do {
          // random point in ([0,1],[0,1])
          point.set(MathUtils.random(), MathUtils.random());
          // Transform point to [ 0.8 x width, 0.8 x height]
          point.x *= width * 0.8f;
          point.y *= height * 0.8f;
          point.add(x + 0.1f * width, y + 0.1f * height);
        } while (isInsideExcludedActor(point));
      }
    } while (areTooClose(points));    
  }

  private boolean approxEquals(float len1, float len2) {
    return Math.abs(len1 - len2) < TOLERANCE;
  }

  public boolean isAvailable() {
    return true;
  }
}