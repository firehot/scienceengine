package com.mazalearn.scienceengine.domains.electromagnetism.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Models a drawing pen which can store multiple samples at 
 * different points in space. 
 * <p/>
 * 
 * @author sridhar
 */
public class Drawing extends Science2DBody {

  private List<List<Vector2>> points = new ArrayList<List<Vector2>>();

  public Drawing(float x, float y, float angle) {
    super(ComponentType.Drawing, x, y, angle);
    getBody().setType(BodyType.DynamicBody);
  }
  
  public void initializeConfigs() {
    super.initializeConfigs();
  }

  @Override
  public void reset() {
    super.reset();
    points.clear();
  }
  
  public void addPointSequence() {
    // Not useful to have a sequence of 0 or 1 points
    if (points.size() > 1 && points.get(points.size() - 1).size() <= 1) {
      points.get(points.size() - 1).clear();
      return;
    }
    points.add(new ArrayList<Vector2>());
  }
  
  public void addPoint(float x, float y) {
    points.get(points.size() - 1).add(new Vector2(x, y));
  }

  public List<List<Vector2>> getPointSequences() {
    return points;
  }
  
  @Override
  public boolean allowsConfiguration() {
    return false;
  }
}
