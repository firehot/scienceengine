package com.mazalearn.scienceengine.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IMagneticField.Consumer;
import com.mazalearn.scienceengine.core.model.IMagneticField.Producer;

public abstract class AbstractScience2DModel implements IScience2DModel {

  protected World box2DWorld;
  protected List<Science2DBody> bodies = new ArrayList<Science2DBody>(); 
  private List<IModelConfig<?>> modelConfigs = new ArrayList<IModelConfig<?>>();

  private boolean isEnabled = true;
  protected int numStepsPerView = 1;
  private List<List<Science2DBody>> circuits;
  List<IMagneticField.Producer> emProducers;
  List<IMagneticField.Consumer> emConsumers;
  Vector2 bField = new Vector2(), totalBField = new Vector2();

  public AbstractScience2DModel() {
    super();
    // Initialize the world for Box2D
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);
    Science2DBody.setBox2DWorld(box2DWorld);    
    this.circuits = new ArrayList<List<Science2DBody>>();
    this.emProducers = new ArrayList<IMagneticField.Producer>();
    this.emConsumers = new ArrayList<IMagneticField.Consumer>();
  }

  @Override
  public void simulateSteps(float delta) {
    if (!isEnabled) return;
    for (int i = 0; i < numStepsPerView; i++) {
      singleStep();
    }
  }

  protected abstract void singleStep();

  public void addBody(Science2DBody science2DBody) {
    bodies.add(science2DBody);
    science2DBody.setModel(this);
    if (science2DBody instanceof IMagneticField.Producer) {
      emProducers.add((Producer) science2DBody);
    }
    if (science2DBody instanceof IMagneticField.Consumer) {
      emConsumers.add((Consumer) science2DBody);
    }
  }
  
  public void propagateField() {
    for (IMagneticField.Consumer consumer: emConsumers) {
      if (!consumer.isActive()) continue;
      // Assume Consumer is not itself a producer
      getBField(consumer.getPosition(), totalBField);
      consumer.setBField(totalBField);
    }
  }

  public void getBField(Vector2 location, Vector2 totalBField) {
    totalBField.set(0, 0);
    for (IMagneticField.Producer producer: emProducers) {
      if (!producer.isActive()) continue;
      // Disallow position identical to producer itself.
      if (producer.getPosition().equals(location)) continue;
      producer.getBField(location, bField);
      totalBField.x += bField.x;
      totalBField.y += bField.y;
    }
  }

  /**
   * Notify all Consumer's of field change.
   */
  public void notifyFieldChange() {
    for (IMagneticField.Consumer consumer: emConsumers) {
      if (consumer.isActive()) {
        consumer.notifyFieldChange();
      }
    }
  }
  
  public void addCircuit(Science2DBody[] bodies) {
    List<Science2DBody> circuit = Arrays.asList(bodies);
    circuits.add(circuit);
    for (Science2DBody science2DBody: circuit) {
      if (science2DBody instanceof ICurrent.Source) {
        notifyCurrentChange((ICurrent.Source) science2DBody);
      }
    }
  }
  
  public List<List<Science2DBody>> getCircuits() {
    return circuits;
  }
  
  public void removeCircuits() {
    // Remove current from all circuits
    for (List<Science2DBody> circuit: circuits) {
      for (Science2DBody science2DBody: circuit) {
        if (science2DBody instanceof ICurrent.Sink) {
          ((ICurrent.Sink) science2DBody).setCurrent(0); 
        }
      }
    }
    circuits.clear();
  }
  
  // There should be only one current source in a circuit.
  // It will push current through all other current sinks in the circuit.
  public void notifyCurrentChange(ICurrent.Source currentSource) {
    float current = currentSource.getCurrent();
    for (List<Science2DBody> circuit: circuits) {
      if (!circuit.contains(currentSource)) continue;
      // Components before currentSource in circuit get negative current
      boolean isNegative = true;
      for (Science2DBody component: circuit) {
        if (component instanceof ICurrent.Sink) {
          ((ICurrent.Sink) component).setCurrent(isNegative ? -current : current);
        } else if (component == currentSource) {
          isNegative = false;
        }
      }
    }
  }

  public List<IModelConfig<?>> getAllConfigs() {
    modelConfigs.clear();
    initializeConfigs(modelConfigs);
    
    for (Science2DBody body: bodies) {
      if (body.isActive()) {
        modelConfigs.addAll(body.getConfigs());
      }
    }
    return Collections.unmodifiableList(modelConfigs);
  }
  
  public IModelConfig<?> getConfig(String name) {
    for (IModelConfig<?> modelConfig: modelConfigs) {
      if (modelConfig.getName().equals(name)) return modelConfig;
    }
    return null;
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }
  
  @Override
  public void enable(boolean enable) {
    isEnabled  = enable;
  }
  
  public World getBox2DWorld() {
    return box2DWorld;
  }

  public abstract void initializeConfigs(List<IModelConfig<?>> modelConfigs);
}