package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.box2d.ScienceActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.controller.IModelConfig;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentSource;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentWire;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.EMField;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Electromagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.SourceCoil;
import com.mazalearn.scienceengine.model.AbstractExperimentModel;

public class ElectroMagnetismModel extends AbstractExperimentModel {
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
    
    ScienceBody electroMagnet = null;
    addBody(barMagnet = new BarMagnet("BarMagnet", emField, 10, 12, 0));
    SourceCoil sourceCoil = new SourceCoil("SourceCoil", 10, 12, 0);
    CurrentSource currentSource = new CurrentSource("Current", 10, 12, 0);
    addBody(currentSource);
    addBody(electroMagnet = new Electromagnet("Electromagnet", emField, sourceCoil, 10, 12, 0));
    addBody(pickupCoil = new PickupCoil("PickupCoil", emField, 23, -4, 0, 2E7f));
    addBody(new Lightbulb("Lightbulb", pickupCoil, 23, 25, 0));
    addBody(new FieldMeter("FieldMeter", emField, 10, 5, 0));
    addBody(new CurrentWire("Wire A", emField, 10, 12, 0));
    addBody(new CurrentWire("Wire B", emField, 14, 12, 0));
    barMagnet.setType(BodyType.DynamicBody);
    addBody(compass = new Compass("Compass", emField, 0, 5, 0));
    compass.setType(BodyType.KinematicBody);
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
    emField.propagateField();
    for (ScienceBody body: bodies) {
      if (body.isActive()) {
        body.singleStep(dt);
      }
    }
  }
  
  @Override
  public void reset() {
    for (ScienceBody body: bodies) {
      body.resetInitial();
    }
    if (joint != null) {
      box2DWorld.destroyJoint(joint);
      joint = null;
    }
    if (mode == Mode.Rotate) {
      jointDef.initialize(barMagnet.getBody(), ScienceBody.getGround(), 
          barMagnet.getWorldPoint(Vector2.Zero));
      joint = box2DWorld.createJoint(jointDef);
    }
  }

  public List<ScienceBody> getBodies() {
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