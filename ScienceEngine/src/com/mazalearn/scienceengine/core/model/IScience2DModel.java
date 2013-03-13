package com.mazalearn.scienceengine.core.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;

public interface IScience2DModel {
  // Reset model to initial conditions
  public void reset();
  // Simulate steps of the model. delta is timeLimit since last invocation.
  public void simulateSteps(float delta);
  // Get all configs of the model
  public Map<String, IModelConfig<?>> getAllConfigs();
  // Box2D World corresponding to model
  public World getBox2DWorld();
  // enable (or disable) the model to progress in simulate steps
  public void enable(boolean enable);
  // whether model is enabled
  public boolean isEnabled();
  
  // Field related interface methods
  // notify of a field change
  public void notifyFieldChange();
  // get magnetic field at a specific position
  public void getBField(Vector2 position, Vector3 fieldVector /* output */);
  
  // Current related interface methods
  // Get list of circuits - each circuit is a list of science2dbodies
  public List<List<CircuitElement>> getCircuits();
  // Remove all circuits
  public void removeCircuits();
  // Create a circuit with specified list of circuit elements
  public void addCircuit(CircuitElement[] circuitElements);
  // notify of a current change at specified currentsource
  public void notifyCurrentChange(ICurrent.Source currentSource);
  // Add body to the model
  Science2DBody addBody(String componentTypeName, 
      float x, float y, float rotation);
  // Prepare model
  void prepareModel();
  // find a config by name
  public IModelConfig<?> getConfig(String configName);
  // Find a body by component type. If there are multiple, return any one.
  public Science2DBody findBody(IComponentType componentType);
  // Find a body by name. If there are multiple, return any one.
  public Science2DBody findBody(String componentTypeName);
  // Bind current parameter values in model to the variables
  public void bindParameterValues(Collection<Variable> variables);
  // List of bodies in the model
  public List<Science2DBody> getBodies();
}
