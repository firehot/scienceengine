package com.mazalearn.scienceengine.experiments.electromagnetism.model;

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
    Vector2 getPosition();
    void setBField(Vector2 bField);
    boolean isActive();
  }
  public interface IProducer {
    Vector2 getBField(Vector2 location, Vector2 bField /* output */);
    boolean isActive();
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
    for (IConsumer consumer: emConsumers) {
      if (!consumer.isActive()) continue;
      Vector2 totalBField = new Vector2(0, 0);
      for (IProducer producer: emProducers) {
        if (producer != consumer && producer.isActive()) {
          producer.getBField(consumer.getPosition(), bField);
          totalBField.x += bField.x;
          totalBField.y += bField.y;
        }
      }
      consumer.setBField(totalBField);
    }
  }

  public void getBField(Vector2 location, Vector2 bField) {
    Vector2 totalBField = new Vector2(0, 0);
    bField.set(0, 0);
    for (IProducer iProducer: emProducers) {
      if (!iProducer.isActive()) continue;
      iProducer.getBField(location, bField);
      totalBField.x += bField.x;
      totalBField.y += bField.y;
    }
  }
}
