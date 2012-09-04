package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.EMField;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.PickupCoil;
import com.mazalearn.scienceengine.model.AbstractExperimentModel;

public class ElectroMagnetismModel extends AbstractExperimentModel {
  private BarMagnet barMagnet;
  private PickupCoil pickupCoil;
  private Lightbulb lightbulb;
  private Compass compass;
  private EMField emField;
  private RevoluteJointDef jointDef = new RevoluteJointDef();
  private List<ScienceBody> bodies = new ArrayList<ScienceBody>(); 
  public enum Mode {Free, Rotate};
  
  private Mode mode = Mode.Free;
  private Joint joint;
    
  public ElectroMagnetismModel() {   
    super();
    emField = new EMField();
    
    bodies.add(barMagnet = new BarMagnet(emField, 10, 12, 0));
    bodies.add(pickupCoil = new PickupCoil(emField, 23, -4, 0, 3000));
    bodies.add(lightbulb = new Lightbulb(pickupCoil, 23, 25, 0));
    barMagnet.setType(BodyType.DynamicBody);
    //bodies.add(compass = new Compass(emField, 10, 5, 0));
    
    reset();
  }

  @Override
  protected void initializeConfigs() {
    modelConfigs.add(new AbstractModelConfig<String>("Mode", 
        "Mode of operation of magnet", Mode.values()) {
      public String getValue() { return getMode(); }
      public void setValue(String value) { setMode(value); }
    });
  }

  @Override
  protected void singleStep() {
    float dt = 0.1f;
    box2DWorld.step(dt, 3, 3);
    emField.propagateField();
    for (ScienceBody body: bodies) {
      body.singleStep(dt);
    }
  }
  
  @Override
  public void reset() {
    for (ScienceBody body: bodies) {
      body.reset();
    }
    if (joint != null) {
      ScienceBody.getBox2DWorld().destroyJoint(joint);
      joint = null;
    }
    if (mode == Mode.Rotate) {
      jointDef.initialize(barMagnet.getBody(), ScienceBody.getGround(), 
          barMagnet.getWorldPoint(Vector2.Zero));
      joint = ScienceBody.getBox2DWorld().createJoint(jointDef);
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