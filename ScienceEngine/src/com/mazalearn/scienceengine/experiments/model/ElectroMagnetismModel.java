package com.mazalearn.scienceengine.experiments.model;

import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.BarMagnet;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.EMField;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.FieldMeter;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.PickupCoil;

public class ElectroMagnetismModel implements IExperimentModel {
  BarMagnet barMagnet;
  PickupCoil pickupCoil;
  FieldMeter fieldMeter;
  EMField emField;
  
  public ElectroMagnetismModel() {
    emField = new EMField();
    
    barMagnet = new BarMagnet(emField);
    barMagnet.setPosition(10, 10);
    barMagnet.setAngle(0);
    
    pickupCoil = new PickupCoil(emField, 0.5);
    pickupCoil.setPosition(11, 11);
    pickupCoil.setAngle(0);
    
    fieldMeter = new FieldMeter(emField);
    Vector2 bforce = new Vector2(0,0);
    fieldMeter.setPosition(11, 11);
    emField.propagateField();
    fieldMeter.getStrength(bforce);
    System.out.println(" bforce = (" + bforce.x + "," + bforce.y);
  }

  public void advance(double d) {
  }
  
  public void simulateStep() {
  }

  @Override
  public void reset() {
  }

}
