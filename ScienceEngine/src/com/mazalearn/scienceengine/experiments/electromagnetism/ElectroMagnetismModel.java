package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.Science2DExperimentModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentSource;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentWire;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.EMField;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Electromagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.PickupCoil;

public class ElectroMagnetismModel extends Science2DExperimentModel {
  private BarMagnet barMagnet;
  private PickupCoil pickupCoil;
  private Compass compass;
  private EMField emField;
  private RevoluteJointDef jointDef = new RevoluteJointDef();
  public enum Mode {Free, Rotate};
  
  private Mode mode = Mode.Free;
  private Joint joint;
    
  public ElectroMagnetismModel() {   
    super();
    emField = new EMField();
    
    Science2DBody electroMagnet = null;
    addBody(barMagnet = new BarMagnet("BarMagnet", 10, 12, 0));
    CurrentSource currentSource = new CurrentSource("Current", 10, 12, 0);
    addBody(currentSource);
    addBody(electroMagnet = new Electromagnet("Electromagnet", 10, 12, 0));
    addBody(pickupCoil = new PickupCoil("PickupCoil", 23, -4, 0, 2E7f));
    addBody(new Lightbulb("Lightbulb", pickupCoil, 23, 25, 0));
    addBody(new CurrentWire("Wire A", 10, 12, 0));
    addBody(new CurrentWire("Wire B", 14, 12, 0));
    barMagnet.setType(BodyType.DynamicBody);
    addBody(compass = new Compass("Compass", 0, 5, 0));
    compass.setType(BodyType.KinematicBody);
    addBody(new FieldMeter("FieldMeter", 10, 5, 0));
    addCircuit(currentSource, electroMagnet);
    
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
    if (joint != null) {
      box2DWorld.destroyJoint(joint);
      joint = null;
    }
    if (mode == Mode.Rotate) {
      jointDef.initialize(barMagnet.getBody(), Science2DBody.getGround(), 
          barMagnet.getWorldPoint(Vector2.Zero));
      joint = box2DWorld.createJoint(jointDef);
    }
  }

  public List<Science2DBody> getBodies() {
    return bodies;
  }
  
  public EMField getEMField() {
    return emField;
  }

  public String getMode() {
    return mode.name();
  }

  public void setMode(String mode) {
    this.mode = Mode.valueOf(mode);
    reset();
  }
}