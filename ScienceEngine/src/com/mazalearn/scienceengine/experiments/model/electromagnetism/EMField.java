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

  public interface Consumer {
    Vector2 getLocation(Vector2 location /* output */);
    void setBField(Vector2 bField);
  }
  public interface Producer {
    Vector2 getBField(Vector2 location, Vector2 bField /* output */);
  }

  List<Producer> emProducers;
  List<Consumer> emConsumers;
  
  public EMField() {
    emProducers = new ArrayList<Producer>();
    emConsumers = new ArrayList<Consumer>();
  }
  
  public void registerProducer(Producer producer) {
    emProducers.add(producer);
  }
  
  public void registerConsumer(Consumer consumer) {
    emConsumers.add(consumer);
  }
  
  public void propagateField() {
    Vector2 bField = new Vector2(0, 0);
    Vector2 location = new Vector2(0, 0);
    for (Consumer consumer: emConsumers) {
      Vector2 totalBField = new Vector2(0, 0);
      for (Producer producer: emProducers) {
        if (producer != consumer) {
          producer.getBField(consumer.getLocation(location), bField);
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
    for (Producer producer: emProducers) {
      producer.getBField(location, bField);
      totalBField.x += bField.x;
      totalBField.y += bField.y;
    }
  }
}
