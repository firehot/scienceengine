package com.mazalearn.scienceengine.experiments.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.box2d.Box2DGroup;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.BarMagnet;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.EMField;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.FieldMeter;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.PickupCoil;

public class ElectroMagnetismModel implements IExperimentModel {
  BarMagnet barMagnet;
  PickupCoil pickupCoil;
  FieldMeter fieldMeter;
  EMField emField;
  private World box2DWorld;
  
  public ElectroMagnetismModel() {
    
    
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);
    ScienceBody.setBox2DWorld(box2DWorld);

    emField = new EMField();
    
    barMagnet = new BarMagnet(emField);
    barMagnet.setPositionAndAngle(12, 12, 0);
    barMagnet.setLinearVelocity(12, 0);
     
    pickupCoil = new PickupCoil(emField, 0.5);
    pickupCoil.setPositionAndAngle(11, 11, 0);
    
    fieldMeter = new FieldMeter(emField);
    Vector2 bforce = new Vector2(0,0);
    fieldMeter.setPositionAndAngle(11, 11, 0);
    for (int i = 0; i < 5; i++) {
      fieldMeter.getStrength(bforce);
      System.out.println(" position = " + barMagnet.getPosition());
      System.out.println(" velocity = " + barMagnet.getLinearVelocity());
      System.out.println(" bforce = " + bforce);
      simulateStep();
    }
  }

  public void advance(double d) {
  }
  
  public void simulateStep() {
    box2DWorld.step(0.1f, 3, 3);
    emField.propagateField();
  }

  @Override
  public void reset() {
  }

}
