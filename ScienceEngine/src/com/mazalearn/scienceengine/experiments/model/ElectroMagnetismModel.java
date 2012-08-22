package com.mazalearn.scienceengine.experiments.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.BarMagnet;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.EMField;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.FieldMeter;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.Lightbulb;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.PickupCoil;

public class ElectroMagnetismModel implements IExperimentModel {
  BarMagnet barMagnet;
  PickupCoil pickupCoil;
  Lightbulb lightbulb;
  EMField emField;
  private World box2DWorld;
  private List<ScienceBody> bodies = new ArrayList<ScienceBody>(); 
  
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
    barMagnet.setAngularDamping(0.2f);
     
    pickupCoil = new PickupCoil(emField, 0.5);
    
    lightbulb = new Lightbulb(pickupCoil);
    
    bodies.add(pickupCoil);
    bodies.add(lightbulb);
    bodies.add(barMagnet);
    reset();
  }

  public void simulateStep() {
    float dt = 0.1f;
    box2DWorld.step(dt, 3, 3);
    emField.propagateField();
    pickupCoil.singleStep(dt);
  }

  @Override
  public void reset() {
    barMagnet.setPositionAndAngle(0, 0, 0);
    pickupCoil.setPositionAndAngle(11, 1, 30);
    lightbulb.setPositionAndAngle(23, 30, 0);
  }
  
  public List<ScienceBody> getBodies() {
    return bodies;
  }
  
  public EMField getEMField() {
    return emField;
  }
}
