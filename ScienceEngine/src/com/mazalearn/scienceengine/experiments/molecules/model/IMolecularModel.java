package com.mazalearn.scienceengine.experiments.molecules.model;

import com.mazalearn.scienceengine.core.model.IScience2DModel;

public interface IMolecularModel extends IScience2DModel {

  // Initialize the molecules configuration (currently uniform random)
  public abstract void reset();

  // The physical time elapsed since beginning of simulation
  public abstract double getSimulatedTime();

  // Energy of the system (PE + KE)
  public abstract double getEnergy();

  // Get ith molecule
  public abstract Molecule getMolecule(int i);
  
  // Get temperature of the system
  public abstract double getTemperature();
  
  // Set temperature of the system.
  // Also sets damping to move the system to stable state at that temperature
  public abstract void setTemperature(double temperature);

  // Set heat level
  public abstract void setHeatingLevel(HeatingLevel heatingLevel);

  // Enumeration for applying heat to the Molecular Model.
  public static enum HeatingLevel {
    Neutral(0), Cold(1), Hot(2);
    private int level;
    private HeatingLevel(int level) { this.level = level; }
    public int level() { return level; }
  }
  // Enumeration for state of the matter
  public static enum State {Solid, Liquid, Gas};
}