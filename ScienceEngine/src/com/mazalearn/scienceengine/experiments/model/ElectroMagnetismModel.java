package com.mazalearn.scienceengine.experiments.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.controller.AbstractConfig;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.BarMagnet;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.EMField;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.Lightbulb;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.PickupCoil;

public class ElectroMagnetismModel extends AbstractExperimentModel {
  private BarMagnet barMagnet;
  private PickupCoil pickupCoil;
  private Lightbulb lightbulb;
  private EMField emField;
  private World box2DWorld;
  private RevoluteJointDef jointDef = new RevoluteJointDef();
  private List<ScienceBody> bodies = new ArrayList<ScienceBody>(); 
  public enum Mode {Free, Rotate};
  
  private Mode mode = Mode.Rotate;
  private Joint joint;
    
  public ElectroMagnetismModel() {   
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);
    ScienceBody.setBox2DWorld(box2DWorld);

    emField = new EMField();
    
    barMagnet = new BarMagnet(emField);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = 0.01f;
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(barMagnet.getWidth(), barMagnet.getHeight());
    fixtureDef.shape = rectangleShape ;
    barMagnet.createFixture(fixtureDef);
    barMagnet.setAngularDamping(0.1f);
     
    pickupCoil = new PickupCoil(emField, 3000);
    
    lightbulb = new Lightbulb(pickupCoil);
    
    bodies.add(pickupCoil);
    bodies.add(lightbulb);
    bodies.add(barMagnet);
    
    reset();
  }

  @Override
  protected void initializeConfigs() {
    modelConfigs.add(new AbstractConfig<String>("Mode", "Mode of operation of magnet", Mode.values()) {
      public String getValue() { return getMode(); }
      public void setValue(String value) { setMode(value); }
    });
  }

  @Override
  protected void singleStep() {
    float dt = 0.1f;
    box2DWorld.step(dt, 3, 3);
    emField.propagateField();
    pickupCoil.singleStep(dt);
  }
  
  @Override
  public void reset() {
    barMagnet.setPositionAndAngle(10, 12, 0);
    pickupCoil.setPositionAndAngle(11, -4, 0);
    lightbulb.setPositionAndAngle(23, 25, 0);
    barMagnet.setLinearVelocity(Vector2.Zero);
    barMagnet.setAngularVelocity(0);
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