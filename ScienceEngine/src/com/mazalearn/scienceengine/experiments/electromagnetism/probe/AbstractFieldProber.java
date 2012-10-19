package com.mazalearn.scienceengine.experiments.electromagnetism.probe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.probe.AbstractScience2DProber;
import com.mazalearn.scienceengine.core.probe.ProbeManager;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;

public abstract class AbstractFieldProber extends AbstractScience2DProber {
  private final Vector2 modelPos = new Vector2();
  private final IScience2DModel model;
  protected FieldMeter fieldMeter;
  protected Science2DActor fieldMeterActor;
 
  protected AbstractFieldProber(IScience2DModel model, ProbeManager probeManager) {
    super(probeManager);
    this.model = model;
    this.fieldMeterActor = (Science2DActor) probeManager.findStageActor("FieldMeter");
    if (fieldMeterActor != null) {
      this.fieldMeter = (FieldMeter) fieldMeterActor.getBody();
      fieldMeterActor.setVisible(false);
    }
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height) {
    super.reinitialize(x, y, width, height);
    fieldMeter.setActive(false);
    fieldMeterActor.setVisible(false);
  }  

  @Override
  public boolean isAvailable() {
    return fieldMeterActor != null;
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
