package com.mazalearn.scienceengine.core.model;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.core.controller.IModelConfig;

public interface IScience2DModel {
  // Reset model to initial conditions
  public void reset();
  // Simulate steps of the model. delta is time since last invocation.
  public void simulateSteps(float delta);
  // Get all configs of the model
  public List<IModelConfig<?>> getAllConfigs();
  // Get a specific named config of the model
  public IModelConfig<?> getConfig(String name);
  public World getBox2DWorld();
  // enable (or disable) the model to progress in simulate steps
  public void enable(boolean enable);
  // whether model is enabled
  public boolean isEnabled();
  // notify of a current change at specified currentsource
  public void notifyCurrentChange(ICurrent.Source currentSource);
  // notify of a field change
  public void notifyFieldChange();
  // get magnetic field at a specific position
  public void getBField(Vector2 position, Vector2 fieldVector /* output */);
  // Get list of circuits - each circuit is a list of science2dbodies
  public List<List<Science2DBody>> getCircuits();
  // Remove all circuits
  public void removeCircuits();
  // Create a circuit with specified list of circuit elements
  public void addCircuit(Science2DBody[] circuitElements);
}
