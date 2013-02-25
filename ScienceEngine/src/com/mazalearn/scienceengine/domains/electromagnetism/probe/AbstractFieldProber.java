package com.mazalearn.scienceengine.domains.electromagnetism.probe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.guru.AbstractScience2DProber;
import com.mazalearn.scienceengine.guru.ITutor;

public abstract class AbstractFieldProber extends AbstractScience2DProber {
  private final Vector2 modelPos = new Vector2();
  protected FieldMeter fieldMeter;
  protected Science2DActor fieldMeterActor;
  private int netSuccesses;
 
  protected AbstractFieldProber(IScience2DController science2DController, 
      ITutor parent, String goal, String id,
      Array<?> components, Array<?> configs, int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2DController, parent, goal, id, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    this.fieldMeterActor = (Science2DActor) science2DController.getView().findActor("FieldMeter");
    if (fieldMeterActor != null) {
      this.fieldMeter = (FieldMeter) fieldMeterActor.getBody();
    }
  }
  
  protected void createFieldMeterSamples(Vector2[] points, Vector2[] bFields) {
    fieldMeter.reset();
    for (int i = 0; i < points.length; i++) {
      fieldMeter.addFieldSample(points[i].x / ScreenComponent.PIXELS_PER_M, 
          points[i].y /  ScreenComponent.PIXELS_PER_M, 
          bFields[i].angle() * MathUtils.degreesToRadians, 
          bFields[i].len());
    }
  }
  
  protected void getBField(Vector2 viewPos, Vector2 bField) {
    modelPos.set(viewPos).mul(1f / ScreenComponent.PIXELS_PER_M);
    science2DController.getModel().getBField(modelPos, bField /* output */);
  }
  
  @Override
  public void prepareToFinish(boolean success) {
    netSuccesses += success ? 1 : -1;
    if (success) {
      guru.showCorrect(getSuccessScore());
    } else {
      guru.showWrong(getFailureScore());
      // TODO: equate isComplete and failure scores
    }
    if (!success) return; // NO Failure exit

    if (netSuccesses >= 2) {
      super.prepareToFinish(success);
    } else {
      teach();
    }
  }
  
  @Override
  public void prepareToTeach(ITutor tutor) {
    super.prepareToTeach(tutor);
    netSuccesses = 0;
  }
}
