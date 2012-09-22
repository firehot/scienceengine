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

public abstract class AbstractExperimentModel implements IExperimentModel {

  protected World box2DWorld;
  protected List<ScienceBody> bodies = new ArrayList<ScienceBody>(); 
  private List<IModelConfig<?>> modelConfigs;

  private boolean isEnabled = true;
  protected int numStepsPerView = 1;
  private List<List<ScienceBody>> circuits;
  List<IMagneticField.Producer> emProducers;
  List<IMagneticField.Consumer> emConsumers;

  public AbstractExperimentModel() {
    super();
    // Initialize the world for Box2D
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);
    ScienceBody.setBox2DWorld(box2DWorld);    
    this.circuits = new ArrayList<List<ScienceBody>>();
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

  public void addBody(ScienceBody scienceBody) {
    bodies.add(scienceBody);
    scienceBody.setModel(this);
    if (scienceBody instanceof IMagneticField.Producer) {
      emProducers.add((Producer) scienceBody);
    }
    if (scienceBody instanceof IMagneticField.Consumer) {
      emConsumers.add((Consumer) scienceBody);
    }
  }
  
  public void propagateField() {
    Vector2 bField = new Vector2(0, 0);
    for (IMagneticField.Consumer consumer: emConsumers) {
      if (!consumer.isActive()) continue;
      Vector2 totalBField = new Vector2(0, 0);
      for (IMagneticField.Producer producer: emProducers) {
        if (producer != consumer && producer.isActive()) {
          producer.getBField(consumer.getPosition(), bField);
          totalBField.x += bField.x;
          totalBField.y += bField.y;
        }
      }
      consumer.setBField(totalBField);
    }
  }

  public void getBField(Vector2 location, Vector2 totalBField) {
    Vector2 bField = new Vector2(0, 0);
    totalBField.set(0, 0);
    for (IMagneticField.Producer producer: emProducers) {
      if (!producer.isActive()) continue;
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
  
  public void addCircuit(ScienceBody... bodies) {
    circuits.add(Arrays.asList(bodies));
  }
  
  // There should be only one current source in a circuit.
  // It will push current through all other current sinks in the circuit.
  public void notifyCurrentChange(ICurrent.Source currentSource) {
    float current = currentSource.getCurrent();
    for (List<ScienceBody> circuit: circuits) {
      if (!circuit.contains(currentSource)) continue;
      for (ScienceBody component: circuit) {
        if (component instanceof ICurrent.Sink) {
          ((ICurrent.Sink) component).updateCurrent(current);
        }
      }
    }
  }

  public List<IModelConfig<?>> getAllConfigs() {
    List<IModelConfig<?>> allConfigs = new ArrayList<IModelConfig<?>>();
    if (modelConfigs == null) {
      modelConfigs = new ArrayList<IModelConfig<?>>();
      initializeConfigs(modelConfigs);
    }
    allConfigs.addAll(modelConfigs);
    for (ScienceBody body: bodies) {
      if (body.isActive()) {
        allConfigs.addAll(body.getConfigs());
      }
    }
    return Collections.unmodifiableList(allConfigs);
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