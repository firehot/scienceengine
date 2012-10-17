package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentCoil;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentSource;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Wire;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Electromagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.PickupCoil;

public class ElectroMagnetismModel extends AbstractScience2DModel {
  private BarMagnet barMagnet;
  private RevoluteJointDef magnetRotationJointDef = new RevoluteJointDef();
  private RevoluteJointDef coilRotationJointDef = new RevoluteJointDef();
  public enum Mode {Free, Rotate};
  
  private Mode mode = Mode.Free;
  private Joint magnetRotationJoint, coilRotationJoint;
  private CurrentCoil currentCoil;
    
  public ElectroMagnetismModel() {   
    super();
    
    addBody(new FieldMeter(10, 5, 0));
    addBody(barMagnet = new BarMagnet(10, 12, 0));
    addBody(new FieldMagnet(8, 8, 0)).setCount(1);
    addBody(new FieldMagnet(12, 8, 0)).setCount(2);
    addBody(new CurrentSource(12, 14, 0)).setCount(1);
    addBody(new CurrentSource(10, 12, 0)).setCount(2);
    addBody(new Electromagnet(10, 12, 0));
    addBody(new PickupCoil(23, -4, 0, 2E7f));
    addBody(new Lightbulb(23, 25, 0));
    addBody(new Wire(8, 12, 0)).setCount(1);
    addBody(new Wire(16, 12, 0)).setCount(2);
    addBody(currentCoil = new CurrentCoil(43, 28, 0));
    addBody(new Compass(0, 5, 0));
    
    for (Science2DBody body: bodies) {
      body.initializeConfigs();
    }
    reset();
  }

  @Override
  public void initializeConfigs(List<IModelConfig<?>> modelConfigs) {
    modelConfigs.add(new AbstractModelConfig<String>("Magnet Mode", 
        "Mode of operation of magnet", Mode.values()) {
      public String getValue() { return getMode(); }
      public void setValue(String value) { setMode(value); }
      public boolean isPossible() { return barMagnet.isActive(); }
    });
  }

  @Override
  protected void singleStep() {
    float dt = Gdx.app.getGraphics().getDeltaTime();
    box2DWorld.step(dt, 3, 3);
    propagateField();
    for (Science2DBody body: bodies) {
      if (body.isActive()) {
        body.singleStep(dt);
      }
    }
  }
  
  @Override
  public void reset() {
    for (Science2DBody body: bodies) {
      body.resetInitial();
    }
    if (magnetRotationJoint != null) {
      box2DWorld.destroyJoint(magnetRotationJoint);
      magnetRotationJoint = null;
    }
    if (mode == Mode.Rotate) {
      magnetRotationJointDef.initialize(barMagnet.getBody(), Science2DBody.getGround(), 
          barMagnet.getWorldPoint(Vector2.Zero));
      magnetRotationJoint = box2DWorld.createJoint(magnetRotationJointDef);
    }
    if (coilRotationJoint == null) {
      coilRotationJointDef.initialize(currentCoil.getBody(), Science2DBody.getGround(), 
          currentCoil.getWorldPoint(Vector2.Zero));
      coilRotationJoint = box2DWorld.createJoint(coilRotationJointDef);
    }
  }

  public List<Science2DBody> getBodies() {
    return bodies;
  }
  
  public String getMode() {
    return mode.name();
  }

  public void setMode(String mode) {
    this.mode = Mode.valueOf(mode);
    reset();
  }
}