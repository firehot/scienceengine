package com.mazalearn.scienceengine.experiments.electromagnetism.probe;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.probe.AbstractProber;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;

public abstract class AbstractFieldProber extends AbstractProber {
  private final Vector2 modelPos = new Vector2();
  private final IExperimentModel model;
  protected FieldMeter fieldMeter;
  protected Science2DActor fieldMeterActor;
 
  protected AbstractFieldProber(IExperimentModel model, List<Actor> actors, Actor dashboard) {
    super(actors, dashboard);
    this.model = model;
    for (Actor actor: actors) {
      if (actor.name == "FieldMeter") {
        this.fieldMeterActor = (Science2DActor) actor;
        this.fieldMeter = (FieldMeter) fieldMeterActor.getBody();
      }
    }
  }
  
  protected void createFieldMeterSamples(Vector2[] points, Vector2[] bFields) {
    fieldMeter.resetInitial();
    for (int i = 0; i < points.length; i++) {
      fieldMeter.addFieldSample(points[i].x / ScienceEngine.PIXELS_PER_M, 
          points[i].y /  ScienceEngine.PIXELS_PER_M, 
          bFields[i].angle() * MathUtils.degreesToRadians, 
          bFields[i].len());
    }
  }
  
  protected void getBField(Vector2 viewPos, Vector2 bField) {
    modelPos.set(viewPos).mul(1f / ScienceEngine.PIXELS_PER_M);
    model.getBField(modelPos, bField /* output */);
  }
}
