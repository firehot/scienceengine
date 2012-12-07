package com.mazalearn.scienceengine.domains.electromagnetism.probe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody.MovementMode;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.guru.AbstractScience2DProber;
import com.mazalearn.scienceengine.guru.Guru;

public abstract class AbstractFieldProber extends AbstractScience2DProber {
  private final Vector2 modelPos = new Vector2();
  protected final IScience2DModel science2DModel;
  protected FieldMeter fieldMeter;
  protected Science2DActor fieldMeterActor;
 
  protected AbstractFieldProber(IScience2DModel science2DModel, Guru guru) {
    super(guru);
    this.science2DModel = science2DModel;
    this.fieldMeterActor = (Science2DActor) guru.findActivityActor("FieldMeter");
    if (fieldMeterActor != null) {
      this.fieldMeter = (FieldMeter) fieldMeterActor.getBody();
    }
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x, y, width, height, probeMode);
    reinitializeConfigs(probeMode);
  }

  private void reinitializeConfigs(boolean probeMode) {
    fieldMeter.setActive(!probeMode);
    fieldMeterActor.setVisible(!probeMode);
    // Make all active elements not movable
    String[] actorNames = 
        new String[] { "BarMagnet", "Wire 1", "Wire 2", "ElectroMagnet"};
    for (String actorName: actorNames) {
      Science2DActor actor = (Science2DActor) guru.findActivityActor(actorName);
      if (actor != null) {
        if (probeMode) {
          actor.setMovementMode(MovementMode.None.name());
        } else {
          actor.getBody().reset();
        }
      }
    }
  }  

  protected void createFieldMeterSamples(Vector2[] points, Vector2[] bFields) {
    fieldMeter.reset();
    for (int i = 0; i < points.length; i++) {
      fieldMeter.addFieldSample(points[i].x / ScienceEngine.PIXELS_PER_M, 
          points[i].y /  ScienceEngine.PIXELS_PER_M, 
          bFields[i].angle() * MathUtils.degreesToRadians, 
          bFields[i].len());
    }
  }
  
  protected void getBField(Vector2 viewPos, Vector2 bField) {
    modelPos.set(viewPos).mul(1f / ScienceEngine.PIXELS_PER_M);
    science2DModel.getBField(modelPos, bField /* output */);
  }
}
