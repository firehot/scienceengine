package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

/**
 * ElectroMagnetic field - There are producers of EMfield and consumers
 * of EMField which all register with this class.
 * At any moment, the field contributions due to the producers at the  consumer 
 * is notified to each consumer.
 * @author sridhar
 *
 */
public class EMField {

  public interface IConsumer {
    Vector2 getLocation(Vector2 location /* output */);
    void setBField(Vector2 bField);
  }
  public interface IProducer {
    Vector2 getBField(Vector2 location, Vector2 bField /* output */);
  }

  List<IProducer> emProducers;
  List<IConsumer> emConsumers;
  
  public EMField() {
    emProducers = new ArrayList<IProducer>();
    emConsumers = new ArrayList<IConsumer>();
  }
  
  public void registerProducer(IProducer iProducer) {
    emProducers.add(iProducer);
  }
  
  public void registerConsumer(IConsumer iConsumer) {
    emConsumers.add(iConsumer);
  }
  
  public void propagateField() {
    Vector2 bField = new Vector2(0, 0);
    Vector2 location = new Vector2(0, 0);
    for (IConsumer iConsumer: emConsumers) {
      Vector2 totalBField = new Vector2(0, 0);
      for (IProducer iProducer: emProducers) {
        if (iProducer != iConsumer) {
          iProducer.getBField(iConsumer.getLocation(location), bField);
          totalBField.x += bField.x;
          totalBField.y += bField.y;
        }
      }
      iConsumer.setBField(totalBField);
    }
  }

  public void getBField(Vector2 location, Vector2 bField) {
    Vector2 totalBField = new Vector2(0, 0);
    bField.set(0, 0);
    for (IProducer iProducer: emProducers) {
      iProducer.getBField(location, bField);
      totalBField.x += bField.x;
      totalBField.y += bField.y;
    }
  }
}
