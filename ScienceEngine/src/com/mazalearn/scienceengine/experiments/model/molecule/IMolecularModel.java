package com.mazalearn.scienceengine.experiments.model.molecule;

public interface IMolecularModel {

  // Initialize the molecules configuration (currently uniform random)
  public abstract void initialize();

  // Simulate n steps
  public abstract void simulateSteps(int n);

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
  public abstract void setHeatingLevel(Heating heating);

  // Enumeration for applying heat to the Molecular Model.
  public enum Heating {
    NEUTRAL(0), COLD(1), HOT(2);
    private int level;
    private Heating(int level) { this.level = level; }
    public int level() { return level; }
  }
}